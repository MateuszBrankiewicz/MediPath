package com.medipath.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.medipath.ui.theme.MediPathTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.MedicalInformation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.medipath.ui.components.InfoCard
import com.medipath.ui.components.MenuCard
import com.medipath.ui.components.Navigation
import com.medipath.ui.components.SearchBar

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediPathTheme {
                HomeScreen()
            }
        }
    }
}

@Composable
fun HomeScreen() {
    Navigation(
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(Modifier.fillMaxSize().padding(top = 13.dp)) {
                    SearchBar { type, query ->
                        println("Kliknięto szukaj: type=$type, query=$query")
                    }
                    Column(
                        modifier = Modifier
                            .padding(vertical = 15.dp, horizontal = 30.dp)
                    ) {
                        Text(
                            text = "Dashboard",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        InfoCard(
                            title = "Prescriptions",
                            label1 = "Lekarz",
                            label2 = "Numer recepty",
                            onClick = { println("Prescriptions clicked") }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        InfoCard(
                            title = "Referrals",
                            label1 = "Lekarz",
                            label2 = "Numer skierowania",
                            onClick = { println("Referrals clicked") }
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MenuCard(
                                icon = Icons.Outlined.List,
                                title = "Visits",
                                onClick = { println("Visits clicked!") },
                                backgroundColor = Color(0xFF6A1B9A),
                                iconColor = Color(0xFF9C4DCC)
                            )

                            MenuCard(
                                icon = Icons.Outlined.MedicalInformation,
                                title = "Medical History",
                                onClick = { println("History clicked!") },
                                backgroundColor = Color(0xFF0277BD),
                                iconColor = Color(0xFF4FC3F7)
                            )

                            MenuCard(
                                icon = Icons.Outlined.Comment,
                                title = "Opinions",
                                onClick = { println("Opinions clicked!") },
                                backgroundColor = Color(0xFFE64A19),
                                iconColor = Color(0xFFFF8A65)
                            )

                            MenuCard(
                                icon = Icons.Outlined.Notifications,
                                title = "Reminders",
                                onClick = { println("Reminders clicked!") },
                                backgroundColor = Color(0xFF2E7D32),
                                iconColor = Color(0xFF66BB6A)
                            )



                        }

                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                        ) {
                            Text(
                                "Upcoming visits",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = "Visit",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        HorizontalDivider(thickness = 2.dp)
                        LazyColumn( //lista wizyt
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(450.dp)
                        ) {
                            items(20) { index ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ){
                                    Text(
                                        text = "Visit $index")
                                    Button(
                                        onClick = {},
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFDE2E2E),
                                            contentColor = MaterialTheme.colorScheme.background
                                        )) {
                                        Text("CANCEL VISIT")
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                }
            }
        },
        onNotificationsClick = {
            //powiadmoenia
        }
    )

}



@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MediPathTheme { HomeScreen() }
}