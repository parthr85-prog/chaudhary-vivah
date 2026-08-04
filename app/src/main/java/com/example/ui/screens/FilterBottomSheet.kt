package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.ui.theme.*
import com.example.ui.viewmodel.MatrimonyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    viewModel: MatrimonyViewModel,
    onDismiss: () -> Unit
) {
    val selectedSubCaste by viewModel.selectedSubCaste.collectAsState()
    val selectedLocality by viewModel.selectedLocality.collectAsState()
    val onlyAadharVerified by viewModel.onlyAadharVerified.collectAsState()
    val excludedGotra by viewModel.excludedGotra.collectAsState()
    val motherVillageSearch by viewModel.motherVillageSearch.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCream,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = RoyalMaroon)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("સમાજ ફિલ્ટર્સ (ચૌધરી મિલન)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RoyalMaroon)
                }

                TextButton(
                    onClick = {
                        viewModel.selectedSubCaste.value = "બધી સબ-કાસ્ટ"
                        viewModel.selectedLocality.value = "બધા પ્રદેશો"
                        viewModel.onlyAadharVerified.value = false
                        viewModel.excludedGotra.value = ""
                        viewModel.motherVillageSearch.value = ""
                    }
                ) {
                    Text("રીસેટ કરો", color = Color.Red)
                }
            }

            HorizontalDivider()

            // Sub-caste Dropdown / Chips
            Text("સબ-કાસ્ટ (શાખા પસંદગી):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SampleData.subCastesList.forEach { sub ->
                    FilterChip(
                        selected = selectedSubCaste == sub,
                        onClick = { viewModel.selectedSubCaste.value = sub },
                        label = { Text(sub, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RoyalMaroon, selectedLabelColor = Color.White)
                    )
                }
            }

            // Region Locality
            Text("પ્રદેશ / રાજ્ય સ્થળ:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SampleData.localitiesList.take(3).forEach { loc ->
                    FilterChip(
                        selected = selectedLocality == loc,
                        onClick = { viewModel.selectedLocality.value = loc },
                        label = { Text(loc, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RoyalMaroon, selectedLabelColor = Color.White)
                    )
                }
            }

            // Gotra Exclusion Rule
            OutlinedTextField(
                value = excludedGotra,
                onValueChange = { viewModel.excludedGotra.value = it },
                label = { Text("ગોત્ર મર્યાદા (વર્જિત ગોત્ર)") },
                placeholder = { Text("દા.ત. ચૌધરી / પટેલ / સારણ") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Mother's Village (Nanihal) search
            OutlinedTextField(
                value = motherVillageSearch,
                onValueChange = { viewModel.motherVillageSearch.value = it },
                label = { Text("મોસાળ (માતાનું જન્મ વતન)") },
                placeholder = { Text("દા.ત. પાલનપુર / ઊંઝા / વિસનગર") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Verified Only
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = onlyAadharVerified,
                    onCheckedChange = { viewModel.onlyAadharVerified.value = it },
                    colors = CheckboxDefaults.colors(checkedColor = VerifiedGreen)
                )
                Text("ફક્ત આધાર પ્રમાણિત પ્રોફાઇલ બતાવો", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = VerifiedGreen)
            }

            // Apply Button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("apply_filters_button"),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ફિલ્ટર્સ લાગુ કરો અને મિલન જુઓ", fontWeight = FontWeight.Bold)
            }
        }
    }
}
