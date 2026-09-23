package com.example.data

import com.example.core.model.LocationData

object CityRepository {
    val CITIES = listOf(
        // --- ANDHRA PRADESH (Complete Districts & Towns) ---
        LocationData("India", "Andhra Pradesh", "Alluri Sitharama Raju", "Paderu", "Paderu", "AP_ASR_01", 18.0645, 82.6667, "Asia/Kolkata", listOf("Paderu", "Alluri Sitharama Raju")),
        LocationData("India", "Andhra Pradesh", "Alluri Sitharama Raju", "Rampachodavaram", "Rampachodavaram", "AP_ASR_02", 17.4475, 81.7583, "Asia/Kolkata", listOf("Rampachodavaram")),
        LocationData("India", "Andhra Pradesh", "Anakapalli", "Anakapalli", "Anakapalli", "AP_AKP_01", 17.6907, 83.0039, "Asia/Kolkata", listOf("Anakapalli")),
        LocationData("India", "Andhra Pradesh", "Anakapalli", "Narsipatnam", "Narsipatnam", "AP_AKP_02", 17.6750, 82.6139, "Asia/Kolkata", listOf("Narsipatnam")),
        LocationData("India", "Andhra Pradesh", "Ananthapuramu", "Anantapur", "Anantapur", "AP_ATP_01", 14.6819, 77.6006, "Asia/Kolkata", listOf("Anantapur", "Ananthapuramu")),
        LocationData("India", "Andhra Pradesh", "Ananthapuramu", "Dharmavaram", "Dharmavaram", "AP_ATP_02", 14.4137, 77.7169, "Asia/Kolkata", listOf("Dharmavaram")),
        LocationData("India", "Andhra Pradesh", "Ananthapuramu", "Hindupur", "Hindupur", "AP_ATP_03", 13.8291, 77.4920, "Asia/Kolkata", listOf("Hindupur")),
        LocationData("India", "Andhra Pradesh", "Ananthapuramu", "Kadiri", "Kadiri", "AP_ATP_04", 14.1167, 78.1667, "Asia/Kolkata", listOf("Kadiri")),
        LocationData("India", "Andhra Pradesh", "Ananthapuramu", "Gooty", "Gooty", "AP_ATP_05", 15.1167, 77.6333, "Asia/Kolkata", listOf("Gooty")),
        LocationData("India", "Andhra Pradesh", "Ananthapuramu", "Rayadurg", "Rayadurg", "AP_ATP_06", 14.7000, 76.8667, "Asia/Kolkata", listOf("Rayadurg")),
        LocationData("India", "Andhra Pradesh", "Ananthapuramu", "Puttaparthi", "Puttaparthi", "AP_ATP_09", 14.1670, 77.8100, "Asia/Kolkata", listOf("Puttaparthi", "Prasanthi Nilayam")),
        LocationData("India", "Andhra Pradesh", "Bapatla", "Bapatla", "Bapatla", "AP_BPT_01", 15.9054, 80.4678, "Asia/Kolkata", listOf("Bapatla")),
        LocationData("India", "Andhra Pradesh", "Bapatla", "Chirala", "Chirala", "AP_BPT_02", 15.8206, 80.3524, "Asia/Kolkata", listOf("Chirala")),
        LocationData("India", "Andhra Pradesh", "Chittoor", "Chittoor", "Chittoor", "AP_CTR_01", 13.2172, 79.1003, "Asia/Kolkata", listOf("Chittoor")),
        LocationData("India", "Andhra Pradesh", "East Godavari", "Rajahmundry", "Rajahmundry", "AP_EG_01", 17.0005, 81.7777, "Asia/Kolkata", listOf("Rajahmundry", "Rajamahendravaram")),
        LocationData("India", "Andhra Pradesh", "Guntur", "Guntur", "Guntur", "AP_GNT_01", 16.3067, 80.4365, "Asia/Kolkata", listOf("Guntur")),
        LocationData("India", "Andhra Pradesh", "Guntur", "Tenali", "Tenali", "AP_GNT_02", 16.2435, 80.6402, "Asia/Kolkata", listOf("Tenali")),
        LocationData("India", "Andhra Pradesh", "Krishna", "Machilipatnam", "Machilipatnam", "AP_KRI_01", 16.1875, 81.1389, "Asia/Kolkata", listOf("Machilipatnam", "Bandar")),
        LocationData("India", "Andhra Pradesh", "Kurnool", "Kurnool", "Kurnool", "AP_KNL_01", 15.8281, 78.0373, "Asia/Kolkata", listOf("Kurnool")),
        LocationData("India", "Andhra Pradesh", "Nandyal", "Nandyal", "Nandyal", "AP_NDL_01", 15.4789, 78.4847, "Asia/Kolkata", listOf("Nandyal")),
        LocationData("India", "Andhra Pradesh", "NTR", "Vijayawada", "Vijayawada", "AP_NTR_01", 16.5062, 80.6480, "Asia/Kolkata", listOf("Vijayawada", "Bezwada")),
        LocationData("India", "Andhra Pradesh", "Palnadu", "Narasaraopet", "Narasaraopet", "AP_PLN_01", 16.2373, 80.0537, "Asia/Kolkata", listOf("Narasaraopet")),
        LocationData("India", "Andhra Pradesh", "Prakasam", "Ongole", "Ongole", "AP_PRK_01", 15.5057, 80.0499, "Asia/Kolkata", listOf("Ongole")),
        LocationData("India", "Andhra Pradesh", "SPSR Nellore", "Nellore", "Nellore", "AP_NLR_01", 14.4426, 79.9865, "Asia/Kolkata", listOf("Nellore", "SPSR Nellore")),
        LocationData("India", "Andhra Pradesh", "Srikakulam", "Srikakulam", "Srikakulam", "AP_SKL_01", 18.2949, 83.8938, "Asia/Kolkata", listOf("Srikakulam")),
        LocationData("India", "Andhra Pradesh", "Tirupati", "Tirupati", "Tirupati", "AP_TPT_01", 13.6288, 79.4192, "Asia/Kolkata", listOf("Tirupati")),
        LocationData("India", "Andhra Pradesh", "Visakhapatnam", "Visakhapatnam", "Visakhapatnam", "AP_VSKP_01", 17.6868, 83.2185, "Asia/Kolkata", listOf("Visakhapatnam", "Vizag")),
        LocationData("India", "Andhra Pradesh", "Vizianagaram", "Vizianagaram", "Vizianagaram", "AP_VZN_01", 18.1124, 83.4157, "Asia/Kolkata", listOf("Vizianagaram")),
        LocationData("India", "Andhra Pradesh", "West Godavari", "Eluru", "Eluru", "AP_WG_01", 16.7107, 81.0952, "Asia/Kolkata", listOf("Eluru")),
        LocationData("India", "Andhra Pradesh", "YSR Kadapa", "Kadapa", "Kadapa", "AP_KDP_01", 14.4673, 78.8242, "Asia/Kolkata", listOf("Kadapa", "Cuddapah")),

        // --- ARUNACHAL PRADESH ---
        LocationData("India", "Arunachal Pradesh", "Papum Pare", "Itanagar", "Itanagar", "AR_PP_01", 27.0844, 93.6053, "Asia/Kolkata", listOf("Itanagar")),
        LocationData("India", "Arunachal Pradesh", "Tawang", "Tawang", "Tawang", "AR_TW_01", 27.5858, 91.8590, "Asia/Kolkata", listOf("Tawang")),
        LocationData("India", "Arunachal Pradesh", "East Siang", "Pasighat", "Pasighat", "AR_ES_01", 28.0671, 95.3256, "Asia/Kolkata", listOf("Pasighat")),
        LocationData("India", "Arunachal Pradesh", "Lower Subansiri", "Ziro", "Ziro", "AR_LS_01", 27.5385, 93.8290, "Asia/Kolkata", listOf("Ziro")),

        // --- ASSAM ---
        LocationData("India", "Assam", "Kamrup Metropolitan", "Guwahati", "Guwahati", "AS_KM_01", 26.1445, 91.7362, "Asia/Kolkata", listOf("Guwahati", "Pragjyotishpura")),
        LocationData("India", "Assam", "Dibrugarh", "Dibrugarh", "Dibrugarh", "AS_DBR_01", 27.4728, 94.9120, "Asia/Kolkata", listOf("Dibrugarh")),
        LocationData("India", "Assam", "Jorhat", "Jorhat", "Jorhat", "AS_JRT_01", 26.7509, 94.2037, "Asia/Kolkata", listOf("Jorhat")),
        LocationData("India", "Assam", "Cachar", "Silchar", "Silchar", "AS_CCR_01", 24.8333, 92.7789, "Asia/Kolkata", listOf("Silchar")),
        LocationData("India", "Assam", "Sonitpur", "Tezpur", "Tezpur", "AS_STP_01", 26.6528, 92.7926, "Asia/Kolkata", listOf("Tezpur")),

        // --- BIHAR ---
        LocationData("India", "Bihar", "Patna", "Patna", "Patna", "BR_PTN_01", 25.5941, 85.1376, "Asia/Kolkata", listOf("Patna", "Pataliputra")),
        LocationData("India", "Bihar", "Gaya", "Gaya", "Gaya", "BR_GAYA_01", 24.7914, 85.0002, "Asia/Kolkata", listOf("Gaya", "Bodh Gaya")),
        LocationData("India", "Bihar", "Bhagalpur", "Bhagalpur", "Bhagalpur", "BR_BGP_01", 25.2425, 87.0172, "Asia/Kolkata", listOf("Bhagalpur")),
        LocationData("India", "Bihar", "Muzaffarpur", "Muzaffarpur", "Muzaffarpur", "BR_MZP_01", 26.1226, 85.3906, "Asia/Kolkata", listOf("Muzaffarpur")),
        LocationData("India", "Bihar", "Darbhanga", "Darbhanga", "Darbhanga", "BR_DBG_01", 26.1542, 85.8918, "Asia/Kolkata", listOf("Darbhanga")),
        LocationData("India", "Bihar", "Nalanda", "Bihar Sharif", "Bihar Sharif", "BR_NAL_01", 25.1972, 85.5186, "Asia/Kolkata", listOf("Bihar Sharif")),
        LocationData("India", "Bihar", "Rohtas", "Sasaram", "Sasaram", "BR_ROT_01", 24.9535, 84.0153, "Asia/Kolkata", listOf("Sasaram")),

        // --- CHHATTISGARH ---
        LocationData("India", "Chhattisgarh", "Raipur", "Raipur", "Raipur", "CG_RPR_01", 21.2514, 81.6296, "Asia/Kolkata", listOf("Raipur")),
        LocationData("India", "Chhattisgarh", "Durg", "Bhilai", "Bhilai", "CG_DRG_01", 21.2000, 81.3833, "Asia/Kolkata", listOf("Bhilai", "Durg")),
        LocationData("India", "Chhattisgarh", "Bilaspur", "Bilaspur", "Bilaspur", "CG_BLS_01", 22.0797, 82.1391, "Asia/Kolkata", listOf("Bilaspur")),
        LocationData("India", "Chhattisgarh", "Korba", "Korba", "Korba", "CG_KRB_01", 22.3595, 82.6803, "Asia/Kolkata", listOf("Korba")),
        LocationData("India", "Chhattisgarh", "Bastar", "Jagdalpur", "Jagdalpur", "CG_BST_01", 19.0743, 82.0253, "Asia/Kolkata", listOf("Jagdalpur")),

        // --- GOA ---
        LocationData("India", "Goa", "North Goa", "Panaji", "Panaji", "GA_NG_01", 15.4909, 73.8278, "Asia/Kolkata", listOf("Panaji", "Panjim")),
        LocationData("India", "Goa", "North Goa", "Mapusa", "Mapusa", "GA_NG_02", 15.5917, 73.8167, "Asia/Kolkata", listOf("Mapusa")),
        LocationData("India", "Goa", "South Goa", "Margao", "Margao", "GA_SG_01", 15.2736, 73.9583, "Asia/Kolkata", listOf("Margao", "Madgaon")),
        LocationData("India", "Goa", "South Goa", "Vasco da Gama", "Vasco da Gama", "GA_SG_02", 15.3997, 73.8122, "Asia/Kolkata", listOf("Vasco da Gama")),

        // --- GUJARAT ---
        LocationData("India", "Gujarat", "Ahmedabad", "Ahmedabad", "Ahmedabad", "GJ_AHM_01", 23.0225, 72.5714, "Asia/Kolkata", listOf("Ahmedabad", "Karnavati")),
        LocationData("India", "Gujarat", "Surat", "Surat", "Surat", "GJ_SRT_01", 21.1702, 72.8311, "Asia/Kolkata", listOf("Surat")),
        LocationData("India", "Gujarat", "Vadodara", "Vadodara", "Vadodara", "GJ_BRD_01", 22.3072, 73.1812, "Asia/Kolkata", listOf("Vadodara", "Baroda")),
        LocationData("India", "Gujarat", "Rajkot", "Rajkot", "Rajkot", "GJ_RJT_01", 22.3039, 70.8022, "Asia/Kolkata", listOf("Rajkot")),
        LocationData("India", "Gujarat", "Bhavnagar", "Bhavnagar", "Bhavnagar", "GJ_BVN_01", 21.7645, 72.1519, "Asia/Kolkata", listOf("Bhavnagar")),
        LocationData("India", "Gujarat", "Jamnagar", "Jamnagar", "Jamnagar", "GJ_JMN_01", 22.4707, 70.0577, "Asia/Kolkata", listOf("Jamnagar")),
        LocationData("India", "Gujarat", "Junagadh", "Junagadh", "Junagadh", "GJ_JNG_01", 21.5222, 70.4579, "Asia/Kolkata", listOf("Junagadh")),
        LocationData("India", "Gujarat", "Gandhinagar", "Gandhinagar", "Gandhinagar", "GJ_GDN_01", 23.2156, 72.6369, "Asia/Kolkata", listOf("Gandhinagar")),
        LocationData("India", "Gujarat", "Anand", "Anand", "Anand", "GJ_AND_01", 22.5530, 72.9234, "Asia/Kolkata", listOf("Anand")),
        LocationData("India", "Gujarat", "Kutch", "Bhuj", "Bhuj", "GJ_KUT_01", 23.2517, 69.6669, "Asia/Kolkata", listOf("Bhuj")),

        // --- HARYANA ---
        LocationData("India", "Haryana", "Faridabad", "Faridabad", "Faridabad", "HR_FRD_01", 28.4089, 77.3178, "Asia/Kolkata", listOf("Faridabad")),
        LocationData("India", "Haryana", "Gurugram", "Gurugram", "Gurugram", "HR_GRG_01", 28.4595, 77.0266, "Asia/Kolkata", listOf("Gurugram", "Gurgaon")),
        LocationData("India", "Haryana", "Panipat", "Panipat", "Panipat", "HR_PNP_01", 29.3909, 76.9635, "Asia/Kolkata", listOf("Panipat")),
        LocationData("India", "Haryana", "Ambala", "Ambala", "Ambala", "HR_AMB_01", 30.3782, 76.7767, "Asia/Kolkata", listOf("Ambala")),
        LocationData("India", "Haryana", "Kurukshetra", "Thanesar", "Thanesar", "HR_KUK_01", 29.9695, 76.8317, "Asia/Kolkata", listOf("Thanesar", "Kurukshetra")),

        // --- HIMACHAL PRADESH ---
        LocationData("India", "Himachal Pradesh", "Shimla", "Shimla", "Shimla", "HP_SML_01", 31.1048, 77.1734, "Asia/Kolkata", listOf("Shimla")),
        LocationData("India", "Himachal Pradesh", "Kangra", "Dharamshala", "Dharamshala", "HP_KNG_01", 32.2190, 76.3234, "Asia/Kolkata", listOf("Dharamshala")),
        LocationData("India", "Himachal Pradesh", "Mandi", "Mandi", "Mandi", "HP_MND_01", 31.7081, 76.9317, "Asia/Kolkata", listOf("Mandi")),
        LocationData("India", "Himachal Pradesh", "Kullu", "Manali", "Manali", "HP_KLL_01", 32.2432, 77.1892, "Asia/Kolkata", listOf("Manali")),

        // --- JHARKHAND ---
        LocationData("India", "Jharkhand", "Ranchi", "Ranchi", "Ranchi", "JH_RNC_01", 23.3441, 85.3096, "Asia/Kolkata", listOf("Ranchi")),
        LocationData("India", "Jharkhand", "East Singhbhum", "Jamshedpur", "Jamshedpur", "JH_ES_01", 22.8046, 86.2029, "Asia/Kolkata", listOf("Jamshedpur", "Tatanagar")),
        LocationData("India", "Jharkhand", "Dhanbad", "Dhanbad", "Dhanbad", "JH_DHN_01", 23.7957, 86.4304, "Asia/Kolkata", listOf("Dhanbad")),
        LocationData("India", "Jharkhand", "Deoghar", "Deoghar", "Deoghar", "JH_DEG_01", 24.4826, 86.7004, "Asia/Kolkata", listOf("Deoghar", "Baidyanath")),

        // --- KARNATAKA ---
        LocationData("India", "Karnataka", "Bengaluru Urban", "Bengaluru", "Bengaluru", "KA_BLR_01", 12.9716, 77.5946, "Asia/Kolkata", listOf("Bengaluru", "Bangalore")),
        LocationData("India", "Karnataka", "Mysuru", "Mysuru", "Mysuru", "KA_MYS_01", 12.2958, 76.6394, "Asia/Kolkata", listOf("Mysuru", "Mysore")),
        LocationData("India", "Karnataka", "Dakshina Kannada", "Mangaluru", "Mangaluru", "KA_DK_01", 12.9141, 74.8560, "Asia/Kolkata", listOf("Mangaluru", "Mangalore")),
        LocationData("India", "Karnataka", "Hubballi-Dharwad", "Hubballi", "Hubballi", "KA_HD_01", 15.3647, 75.1240, "Asia/Kolkata", listOf("Hubballi", "Hubli")),

        // --- KERALA ---
        LocationData("India", "Kerala", "Thiruvananthapuram", "Thiruvananthapuram", "Thiruvananthapuram", "KL_TVM_01", 8.5241, 76.9366, "Asia/Kolkata", listOf("Thiruvananthapuram", "Trivandrum")),
        LocationData("India", "Kerala", "Ernakulam", "Kochi", "Kochi", "KL_EKM_01", 9.9312, 76.2673, "Asia/Kolkata", listOf("Kochi", "Cochin")),
        LocationData("India", "Kerala", "Kozhikode", "Kozhikode", "Kozhikode", "KL_KKD_01", 11.2588, 75.7804, "Asia/Kolkata", listOf("Kozhikode", "Calicut")),
        LocationData("India", "Kerala", "Thrissur", "Thrissur", "Thrissur", "KL_TSR_01", 10.5276, 76.2144, "Asia/Kolkata", listOf("Thrissur")),

        // --- MADHYA PRADESH ---
        LocationData("India", "Madhya Pradesh", "Indore", "Indore", "Indore", "MP_IND_01", 22.7196, 75.8577, "Asia/Kolkata", listOf("Indore")),
        LocationData("India", "Madhya Pradesh", "Bhopal", "Bhopal", "Bhopal", "MP_BPL_01", 23.2599, 77.4126, "Asia/Kolkata", listOf("Bhopal")),
        LocationData("India", "Madhya Pradesh", "Jabalpur", "Jabalpur", "Jabalpur", "MP_JBL_01", 23.1815, 79.9864, "Asia/Kolkata", listOf("Jabalpur")),
        LocationData("India", "Madhya Pradesh", "Gwalior", "Gwalior", "Gwalior", "MP_GWL_01", 26.2183, 78.1828, "Asia/Kolkata", listOf("Gwalior")),
        LocationData("India", "Madhya Pradesh", "Ujjain", "Ujjain", "Ujjain", "MP_UJN_01", 23.1765, 75.7885, "Asia/Kolkata", listOf("Ujjain", "Avantika")),

        // --- MAHARASHTRA ---
        LocationData("India", "Maharashtra", "Mumbai City", "Mumbai", "Mumbai", "MH_MUM_01", 19.0760, 72.8777, "Asia/Kolkata", listOf("Mumbai", "Bombay")),
        LocationData("India", "Maharashtra", "Pune", "Pune", "Pune", "MH_PUN_01", 18.5204, 73.8567, "Asia/Kolkata", listOf("Pune")),
        LocationData("India", "Maharashtra", "Nagpur", "Nagpur", "Nagpur", "MH_NGP_01", 21.1458, 79.0882, "Asia/Kolkata", listOf("Nagpur")),
        LocationData("India", "Maharashtra", "Nashik", "Nashik", "Nashik", "MH_NSK_01", 19.9975, 73.7898, "Asia/Kolkata", listOf("Nashik")),
        LocationData("India", "Maharashtra", "Chhatrapati Sambhajinagar", "Aurangabad", "Aurangabad", "MH_AUR_01", 19.8762, 75.3433, "Asia/Kolkata", listOf("Aurangabad", "Chhatrapati Sambhajinagar")),

        // --- MANIPUR ---
        LocationData("India", "Manipur", "Imphal West", "Imphal", "Imphal", "MN_IMP_01", 24.8170, 93.9368, "Asia/Kolkata", listOf("Imphal")),

        // --- MEGHALAYA ---
        LocationData("India", "Meghalaya", "East Khasi Hills", "Shillong", "Shillong", "ML_SHL_01", 25.5788, 91.8933, "Asia/Kolkata", listOf("Shillong")),

        // --- MIZORAM ---
        LocationData("India", "Mizoram", "Aizawl", "Aizawl", "Aizawl", "MZ_AIZ_01", 23.7271, 92.7176, "Asia/Kolkata", listOf("Aizawl")),

        // --- NAGALAND ---
        LocationData("India", "Nagaland", "Kohima", "Kohima", "Kohima", "NL_KHM_01", 25.6751, 94.1086, "Asia/Kolkata", listOf("Kohima")),
        LocationData("India", "Nagaland", "Dimapur", "Dimapur", "Dimapur", "NL_DMP_01", 25.9094, 93.7263, "Asia/Kolkata", listOf("Dimapur")),

        // --- ODISHA ---
        LocationData("India", "Odisha", "Khordha", "Bhubaneswar", "Bhubaneswar", "OD_BBS_01", 20.2961, 85.8245, "Asia/Kolkata", listOf("Bhubaneswar")),
        LocationData("India", "Odisha", "Cuttack", "Cuttack", "Cuttack", "OD_CTC_01", 20.4625, 85.8828, "Asia/Kolkata", listOf("Cuttack")),
        LocationData("India", "Odisha", "Puri", "Puri", "Puri", "OD_PUR_01", 19.8135, 85.8312, "Asia/Kolkata", listOf("Puri", "Jagannath Dham")),

        // --- PUNJAB ---
        LocationData("India", "Punjab", "Ludhiana", "Ludhiana", "Ludhiana", "PB_LDH_01", 30.9010, 75.8573, "Asia/Kolkata", listOf("Ludhiana")),
        LocationData("India", "Punjab", "Amritsar", "Amritsar", "Amritsar", "PB_ASR_01", 31.6340, 74.8723, "Asia/Kolkata", listOf("Amritsar")),
        LocationData("India", "Punjab", "Jalandhar", "Jalandhar", "Jalandhar", "PB_JAL_01", 31.3260, 75.5762, "Asia/Kolkata", listOf("Jalandhar")),
        LocationData("India", "Punjab", "Patiala", "Patiala", "Patiala", "PB_PTA_01", 30.3398, 76.3869, "Asia/Kolkata", listOf("Patiala")),

        // --- RAJASTHAN ---
        LocationData("India", "Rajasthan", "Jaipur", "Jaipur", "Jaipur", "RJ_JAI_01", 26.9124, 75.7873, "Asia/Kolkata", listOf("Jaipur", "Pink City")),
        LocationData("India", "Rajasthan", "Jodhpur", "Jodhpur", "Jodhpur", "RJ_JDH_01", 26.2389, 73.0243, "Asia/Kolkata", listOf("Jodhpur")),
        LocationData("India", "Rajasthan", "Udaipur", "Udaipur", "Udaipur", "RJ_UDP_01", 24.5854, 73.7125, "Asia/Kolkata", listOf("Udaipur")),
        LocationData("India", "Rajasthan", "Kota", "Kota", "Kota", "RJ_KTA_01", 25.2138, 75.8648, "Asia/Kolkata", listOf("Kota")),
        LocationData("India", "Rajasthan", "Ajmer", "Ajmer", "Ajmer", "RJ_AJM_01", 26.4499, 74.6399, "Asia/Kolkata", listOf("Ajmer")),

        // --- SIKKIM ---
        LocationData("India", "Sikkim", "East Sikkim", "Gangtok", "Gangtok", "SK_GTK_01", 27.3389, 88.6065, "Asia/Kolkata", listOf("Gangtok")),

        // --- TAMIL NADU ---
        LocationData("India", "Tamil Nadu", "Chennai", "Chennai", "Chennai", "TN_CHN_01", 13.0827, 80.2707, "Asia/Kolkata", listOf("Chennai", "Madras")),
        LocationData("India", "Tamil Nadu", "Coimbatore", "Coimbatore", "Coimbatore", "TN_CBE_01", 11.0168, 76.9558, "Asia/Kolkata", listOf("Coimbatore")),
        LocationData("India", "Tamil Nadu", "Madurai", "Madurai", "Madurai", "TN_MDU_01", 9.9252, 78.1198, "Asia/Kolkata", listOf("Madurai")),
        LocationData("India", "Tamil Nadu", "Tiruchirappalli", "Tiruchirappalli", "Tiruchirappalli", "TN_TRY_01", 10.7905, 78.7047, "Asia/Kolkata", listOf("Tiruchirappalli", "Trichy")),

        // --- TELANGANA ---
        LocationData("India", "Telangana", "Hyderabad", "Hyderabad", "Hyderabad", "TG_HYD_01", 17.3850, 78.4867, "Asia/Kolkata", listOf("Hyderabad")),
        LocationData("India", "Telangana", "Warangal", "Warangal", "Warangal", "TG_WGL_01", 17.9689, 79.5941, "Asia/Kolkata", listOf("Warangal")),
        LocationData("India", "Telangana", "Nizamabad", "Nizamabad", "Nizamabad", "TG_NZB_01", 18.6725, 78.0940, "Asia/Kolkata", listOf("Nizamabad")),

        // --- TRIPURA ---
        LocationData("India", "Tripura", "West Tripura", "Agartala", "Agartala", "TR_AGT_01", 23.8315, 91.2868, "Asia/Kolkata", listOf("Agartala")),

        // --- UTTAR PRADESH ---
        LocationData("India", "Uttar Pradesh", "Lucknow", "Lucknow", "Lucknow", "UP_LKO_01", 26.8467, 80.9462, "Asia/Kolkata", listOf("Lucknow")),
        LocationData("India", "Uttar Pradesh", "Kanpur Nagar", "Kanpur", "Kanpur", "UP_KNP_01", 26.4499, 80.3319, "Asia/Kolkata", listOf("Kanpur")),
        LocationData("India", "Uttar Pradesh", "Varanasi", "Varanasi", "Varanasi", "UP_VNS_01", 25.3176, 82.9739, "Asia/Kolkata", listOf("Varanasi", "Kashi", "Banaras")),
        LocationData("India", "Uttar Pradesh", "Agra", "Agra", "Agra", "UP_AGR_01", 27.1767, 78.0081, "Asia/Kolkata", listOf("Agra")),
        LocationData("India", "Uttar Pradesh", "Prayagraj", "Prayagraj", "Prayagraj", "UP_PRY_01", 25.4358, 81.8463, "Asia/Kolkata", listOf("Prayagraj", "Allahabad")),
        LocationData("India", "Uttar Pradesh", "Ayodhya", "Ayodhya", "Ayodhya", "UP_AYO_01", 26.7922, 82.1998, "Asia/Kolkata", listOf("Ayodhya", "Faizabad")),
        LocationData("India", "Uttar Pradesh", "Mathura", "Mathura", "Mathura", "UP_MTH_01", 27.4924, 77.6737, "Asia/Kolkata", listOf("Mathura")),
        LocationData("India", "Uttar Pradesh", "Mathura", "Vrindavan", "Vrindavan", "UP_VRN_01", 27.5706, 77.6985, "Asia/Kolkata", listOf("Vrindavan")),

        // --- UTTARAKHAND ---
        LocationData("India", "Uttarakhand", "Dehradun", "Dehradun", "Dehradun", "UK_DDN_01", 30.3165, 78.0322, "Asia/Kolkata", listOf("Dehradun")),
        LocationData("India", "Uttarakhand", "Haridwar", "Haridwar", "Haridwar", "UK_HDW_01", 29.9457, 78.1642, "Asia/Kolkata", listOf("Haridwar")),
        LocationData("India", "Uttarakhand", "Dehradun", "Rishikesh", "Rishikesh", "UK_RSK_01", 30.0869, 78.2676, "Asia/Kolkata", listOf("Rishikesh")),

        // --- WEST BENGAL ---
        LocationData("India", "West Bengal", "Kolkata", "Kolkata", "Kolkata", "WB_CCU_01", 22.5726, 88.3639, "Asia/Kolkata", listOf("Kolkata", "Calcutta")),
        LocationData("India", "West Bengal", "Howrah", "Howrah", "Howrah", "WB_HWH_01", 22.5958, 88.2636, "Asia/Kolkata", listOf("Howrah")),
        LocationData("India", "West Bengal", "Darjeeling", "Darjeeling", "Darjeeling", "WB_DJ_01", 27.0410, 88.2663, "Asia/Kolkata", listOf("Darjeeling")),

        // --- UNION TERRITORIES ---
        LocationData("India", "Andaman and Nicobar Islands", "South Andaman", "Port Blair", "Port Blair", "AN_PB_01", 11.6234, 92.7265, "Asia/Kolkata", listOf("Port Blair")),
        LocationData("India", "Chandigarh", "Chandigarh", "Chandigarh", "Chandigarh", "CH_CHD_01", 30.7333, 76.7794, "Asia/Kolkata", listOf("Chandigarh")),
        LocationData("India", "Dadra and Nagar Haveli and Daman and Diu", "Daman", "Daman", "Daman", "DN_DMN_01", 20.4283, 72.8397, "Asia/Kolkata", listOf("Daman")),
        LocationData("India", "Delhi", "New Delhi", "New Delhi", "New Delhi", "DL_NDL_01", 28.6139, 77.2090, "Asia/Kolkata", listOf("New Delhi", "Delhi")),
        LocationData("India", "Jammu and Kashmir", "Srinagar", "Srinagar", "Srinagar", "JK_SRI_01", 34.0837, 74.7973, "Asia/Kolkata", listOf("Srinagar")),
        LocationData("India", "Jammu and Kashmir", "Jammu", "Jammu", "Jammu", "JK_JAM_01", 32.7266, 74.8570, "Asia/Kolkata", listOf("Jammu")),
        LocationData("India", "Ladakh", "Leh", "Leh", "Leh", "LA_LEH_01", 34.1526, 77.5771, "Asia/Kolkata", listOf("Leh")),
        LocationData("India", "Lakshadweep", "Kavaratti", "Kavaratti", "Kavaratti", "LD_KVR_01", 10.5667, 72.6417, "Asia/Kolkata", listOf("Kavaratti")),
        LocationData("India", "Puducherry", "Puducherry", "Puducherry", "Puducherry", "PY_PDY_01", 11.9416, 79.8083, "Asia/Kolkata", listOf("Puducherry", "Pondicherry"))
    )

    fun getStatesAndUTs(): List<String> = CITIES.map { it.state }.distinct().sorted()

    fun getDistrictsForState(state: String): List<String> = CITIES.filter { it.state.equals(state, true) }.map { it.district }.distinct().sorted()

    fun getTownsForDistrict(state: String, district: String): List<LocationData> = CITIES.filter { it.state.equals(state, true) && it.district.equals(district, true) }.sortedBy { it.talukaOrTown }

    fun searchLocations(query: String): List<LocationData> {
        if (query.isBlank()) return CITIES
        return CITIES.filter { city ->
            city.talukaOrTown.contains(query, ignoreCase = true) ||
            city.district.contains(query, ignoreCase = true) ||
            city.state.contains(query, ignoreCase = true) ||
            city.country.contains(query, ignoreCase = true) ||
            city.cityName.contains(query, ignoreCase = true) ||
            (city.officialLocationCode?.contains(query, ignoreCase = true) == true) ||
            city.alternateNames.any { it.contains(query, ignoreCase = true) }
        }
    }

    fun getDefaultCity(): LocationData = CITIES.first()
}
