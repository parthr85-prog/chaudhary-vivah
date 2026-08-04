package com.example.data

object IndiaLocationData {
    val statesList = listOf(
        "Gujarat",
        "Rajasthan",
        "Maharashtra",
        "Madhya Pradesh",
        "Haryana",
        "Delhi NCR",
        "Uttar Pradesh",
        "Punjab",
        "Bihar",
        "Karnataka",
        "Tamil Nadu",
        "Other State"
    )

    private val districtsMap = mapOf(
        "Gujarat" to listOf(
            "Banaskantha", "Mehsana", "Patan", "Sabarkantha", "Gandhinagar",
            "Ahmedabad", "Surat", "Rajkot", "Vadodara", "Kutch", "Anand", "Kheda",
            "Panchmahal", "Jamnagar", "Junagadh", "Amreli", "Bhavnagar", "Porbandar",
            "Navsari", "Valsad", "Bharuch", "Tapi", "Aravalli", "Mahisagar", "Botad",
            "Gir Somnath", "Morbi", "Devbhumi Dwarka"
        ),
        "Rajasthan" to listOf(
            "Jalore", "Sirohi", "Pali", "Barmer", "Jodhpur", "Udaipur", "Jaipur",
            "Ajmer", "Nagaur", "Bikaner", "Jhunjhunu", "Sikar", "Churu", "Kota",
            "Chittorgarh", "Bhilwara", "Rajsamand"
        ),
        "Maharashtra" to listOf(
            "Mumbai", "Pune", "Nagpur", "Nashik", "Thane", "Aurangabad", "Solapur", "Jalgaon"
        ),
        "Madhya Pradesh" to listOf(
            "Indore", "Bhopal", "Ujjain", "Gwalior", "Jabalpur", "Ratlam", "Mandsaur", "Neemuch"
        ),
        "Haryana" to listOf(
            "Bhiwani", "Hisar", "Rohtak", "Gurugram", "Faridabad", "Karnal", "Ambala", "Sirsa", "Fatehabad"
        ),
        "Delhi NCR" to listOf(
            "New Delhi", "North Delhi", "South Delhi", "East Delhi", "West Delhi"
        ),
        "Uttar Pradesh" to listOf(
            "Lucknow", "Noida", "Ghaziabad", "Agra", "Varanasi", "Meerut", "Mathura"
        )
    )

    private val subDistrictsMap = mapOf(
        "Banaskantha" to listOf("Palanpur", "Deesa", "Vadgam", "Kankrej", "Dantiwada", "Dhanera", "Tharad", "Wav", "Bhabhar", "Lakhani", "Danta", "Diyodar", "Suigam", "Amirgadh"),
        "Mehsana" to listOf("Mehsana", "Kadi", "Unjha", "Visnagar", "Kheralu", "Vadnagar", "Becharaji", "Vijapur", "Satlasana", "Gozaria", "Jotana"),
        "Patan" to listOf("Patan", "Sidhpur", "Radhanpur", "Chanasma", "Harij", "Sami", "Santalpur", "Shankheshwar", "Saraswati"),
        "Sabarkantha" to listOf("Himatnagar", "Idar", "Khedbrahma", "Vadali", "Talod", "Prantij", "Vijaynagar"),
        "Gandhinagar" to listOf("Gandhinagar", "Kalol", "Dehgam", "Mansa"),
        "Ahmedabad" to listOf("Ahmedabad City", "Daskroi", "Sanand", "Bavla", "Dholka", "Viramgam", "Mandal", "Detroj"),
        "Surat" to listOf("Surat City", "Olpad", "Chorasi", "Kamrej", "Bardoli", "Mandvi"),
        "Rajkot" to listOf("Rajkot City", "Gondal", "Jetpur", "Morbi", "Dhoraji", "Jasdan"),
        "Vadodara" to listOf("Vadodara City", "Padra", "Dabhoi", "Karjan", "Savli"),
        "Kutch" to listOf("Bhuj", "Anjar", "Gandhidham", "Mandvi", "Mundra", "Nakhatrana", "Rapar"),
        "Jalore" to listOf("Sanchore", "Jalore", "Bhinmal", "Ahore", "Raniwara", "Bagoda", "Sayla"),
        "Sirohi" to listOf("Sirohi", "Sheoganj", "Pindwara", "Abu Road", "Reodar"),
        "Pali" to listOf("Pali", "Sumerpur", "Bali", "Sojat", "Marwar Junction", "Rohat"),
        "Barmer" to listOf("Barmer", "Balotra", "Gudamalani", "Chohtan", "Siwana", "Baytu"),
        "Jodhpur" to listOf("Jodhpur", "Luni", "Bilara", "Osian", "Phalodi"),
        "Bhiwani" to listOf("Bhiwani", "Bawani Khera", "Tosham", "Siwani", "Loharu")
    )

    fun getDistricts(state: String): List<String> {
        return districtsMap[state] ?: listOf("Central District", "North District", "South District", "East District", "West District", "Other District")
    }

    fun getSubDistricts(district: String): List<String> {
        return subDistrictsMap[district] ?: listOf("Taluka Headquarter", "North Sub-district", "South Sub-district", "Central Sub-district", "Other Taluka")
    }
}
