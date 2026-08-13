import pty
import os
import sys
import time
import re

log_file = "login_output.log"
url_file = "auth_url.txt"

pid, master = pty.fork()

if pid == 0:
    # Child process
    os.execvp("npx", ["npx", "firebase-tools", "login", "--no-localhost"])
else:
    # Parent process
    with open(log_file, "w") as f:
        buffer = ""
        answered_mcp = False
        answered_telemetry = False
        start_time = time.time()
        while time.time() - start_time < 300: # stay open for 5 mins
            try:
                data = os.read(master, 1024).decode('utf-8', errors='ignore')
                if not data:
                    break
                f.write(data)
                f.flush()
                buffer += data

                if "Enable Gemini" in buffer and "(Y/n)" in buffer and not answered_mcp:
                    os.write(master, b"y\n")
                    answered_mcp = True
                    buffer = ""

                if "Allow Firebase to collect" in buffer and "(Y/n)" in buffer and not answered_telemetry:
                    os.write(master, b"y\n")
                    answered_telemetry = True
                    buffer = ""

                # Check for authorization code prompt or URL
                urls = re.findall(r'https://accounts\.google\.com/o/oauth2/auth[^\s]+', buffer)
                if urls:
                    with open(url_file, "w") as u_f:
                        u_f.write(urls[0])

            except OSError:
                break
