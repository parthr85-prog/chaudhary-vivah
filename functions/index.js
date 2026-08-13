const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

/**
 * Helper function to send push notification to target user tokens
 */
async function sendPushToUser(userId, notificationPayload, dataPayload) {
  if (!userId) return;

  try {
    // 1. Save in-app notification document in notifications collection
    try {
      await db.collection("notifications").add({
        id: "notif_" + Date.now() + "_" + Math.floor(Math.random() * 10000),
        userId: userId,
        targetId: userId,
        title: notificationPayload.title || "ચૌધરી વિવાહ સંસ્થાન",
        message: notificationPayload.body || "",
        type: dataPayload.type || "GENERAL",
        targetIdParam: dataPayload.senderId || "",
        isRead: false,
        timestamp: Date.now()
      });
    } catch (e) {
      console.error(`Error writing notification document for user ${userId}:`, e);
    }

    const userDoc = await db.collection("profiles").doc(userId).get();
    if (!userDoc.exists) return;

    const userData = userDoc.data();
    let tokens = [];

    if (Array.isArray(userData.fcmTokens) && userData.fcmTokens.length > 0) {
      tokens = userData.fcmTokens.filter(t => typeof t === "string" && t.trim().length > 0);
    } else if (userData.lastFcmToken && typeof userData.lastFcmToken === "string") {
      tokens = [userData.lastFcmToken.trim()];
    }

    // Deduplicate tokens
    tokens = [...new Set(tokens)];

    if (tokens.length === 0) {
      console.log(`No FCM tokens found for target user ${userId}`);
      return;
    }

    const message = {
      notification: {
        title: notificationPayload.title || "ચૌધરી વિવાહ સંસ્થાન",
        body: notificationPayload.body || ""
      },
      data: {
        ...dataPayload,
        title: notificationPayload.title || "",
        body: notificationPayload.body || ""
      },
      tokens: tokens,
      android: {
        priority: "high",
        notification: {
          channelId: "chaudhary_vivah_general_channel",
          sound: "default",
          priority: "high"
        }
      }
    };

    const response = await admin.messaging().sendEachForMulticast(message);
    console.log(`Successfully sent message to ${response.successCount} devices for user ${userId}`);

    // Clean up stale/invalid tokens
    const tokensToRemove = [];
    response.responses.forEach((resp, idx) => {
      if (!resp.success) {
        const error = resp.error;
        if (
          error &&
          (error.code === "messaging/invalid-registration-token" ||
            error.code === "messaging/registration-token-not-registered")
        ) {
          tokensToRemove.push(tokens[idx]);
        }
      }
    });

    if (tokensToRemove.length > 0) {
      console.log(`Removing ${tokensToRemove.length} stale FCM tokens for user ${userId}`);
      await db.collection("profiles").doc(userId).update({
        fcmTokens: admin.firestore.FieldValue.arrayRemove(...tokensToRemove)
      });
    }
  } catch (error) {
    console.error(`Error sending push notification to user ${userId}:`, error);
  }
}

/**
 * Trigger on new Chat Message creation
 */
exports.onChatMessageCreated = functions.firestore
  .document("chat_messages/{messageId}")
  .onCreate(async (snapshot, context) => {
    const msg = snapshot.data();
    if (!msg) return null;

    const senderId = msg.senderId;
    const receiverId = msg.receiverId;
    const rawMessage = msg.message || "";

    if (!senderId || !receiverId) return null;

    // Fetch sender profile name
    let senderName = "ચૌધરી મિલન સાથી";
    try {
      const senderDoc = await db.collection("profiles").doc(senderId).get();
      if (senderDoc.exists && senderDoc.data().fullName) {
        senderName = senderDoc.data().fullName;
      }
    } catch (e) {
      console.error("Error fetching sender profile:", e);
    }

    // Prepare message preview text
    let bodyPreview = rawMessage;
    if (msg.isVoiceNote || rawMessage.includes("🎙️")) {
      bodyPreview = "🎙️ વોઇસ મેસેજ મોકલ્યો છે.";
    } else if (bodyPreview.length > 100) {
      bodyPreview = bodyPreview.substring(0, 97) + "...";
    }

    return sendPushToUser(
      receiverId,
      {
        title: `💬 ${senderName}`,
        body: bodyPreview
      },
      {
        type: "NEW_MESSAGE",
        senderId: senderId,
        receiverId: receiverId,
        chatId: senderId,
        messageId: context.params.messageId
      }
    );
  });

/**
 * Trigger on Interest Request creation or status update
 */
exports.onInterestRequestWritten = functions.firestore
  .document("interest_requests/{requestId}")
  .onWrite(async (change, context) => {
    const beforeData = change.before.exists ? change.before.data() : null;
    const afterData = change.after.exists ? change.after.data() : null;

    if (!afterData) return null; // Deleted document

    const senderId = afterData.senderId;
    const receiverId = afterData.receiverId;
    const status = afterData.status || "PENDING";
    const oldStatus = beforeData ? beforeData.status : null;

    if (!senderId || !receiverId) return null;

    // 1. New Interest Request Sent
    if (!beforeData && status === "PENDING") {
      let senderName = "એક સભ્ય";
      try {
        const senderDoc = await db.collection("profiles").doc(senderId).get();
        if (senderDoc.exists && senderDoc.data().fullName) {
          senderName = senderDoc.data().fullName;
        }
      } catch (e) {}

      return sendPushToUser(
        receiverId,
        {
          title: "❤️ નવો રસ-પ્રસ્તાવ (New Interest)",
          body: `${senderName} એ તમને વિવાહ રસ-પ્રસ્તાવ મોકલ્યો છે.`
        },
        {
          type: "NEW_INTEREST",
          requestId: context.params.requestId,
          senderId: senderId,
          receiverId: receiverId
        }
      );
    }

    // 2. Interest Request Accepted
    if (oldStatus !== "ACCEPTED" && status === "ACCEPTED") {
      let acceptingUserName = "સભ્ય";
      try {
        const receiverDoc = await db.collection("profiles").doc(receiverId).get();
        if (receiverDoc.exists && receiverDoc.data().fullName) {
          acceptingUserName = receiverDoc.data().fullName;
        }
      } catch (e) {}

      return sendPushToUser(
        senderId,
        {
          title: "❤️ રસ-પ્રસ્તાવ સ્વીકારાયો! (Interest Accepted)",
          body: `${acceptingUserName} એ તમારો વિવાહ રસ-પ્રસ્તાવ સ્વીકારી લીધો છે. હવે તમે ચેટ કરી શકો છો.`
        },
        {
          type: "INTEREST_ACCEPTED",
          requestId: context.params.requestId,
          senderId: receiverId,
          receiverId: senderId
        }
      );
    }

    // 3. Interest Request Rejected
    if (oldStatus !== "REJECTED" && status === "REJECTED") {
      let decliningUserName = "સભ્ય";
      try {
        const receiverDoc = await db.collection("profiles").doc(receiverId).get();
        if (receiverDoc.exists && receiverDoc.data().fullName) {
          decliningUserName = receiverDoc.data().fullName;
        }
      } catch (e) {}

      return sendPushToUser(
        senderId,
        {
          title: "રસ-પ્રસ્તાવ અસ્વીકૃત (Interest Declined)",
          body: `${decliningUserName} એ તમારો રસ-પ્રસ્તાવ અસ્વીકાર કર્યો છે.`
        },
        {
          type: "INTEREST_REJECTED",
          requestId: context.params.requestId,
          senderId: receiverId,
          receiverId: senderId
        }
      );
    }

    return null;
  });
