const { MongoClient, ObjectId } = require('mongodb');
const { faker } = require('@faker-js/faker'); 
const argon2 = require('argon2');

const mongoUri = 'mongodb://root:secret@mongodb:27017/medipath?authSource=admin';
const client = new MongoClient(mongoUri);

// --- QUANTITY CONFIG ---
const NUM_INSTITUTIONS = 20;
const DOCTORS_PER_INSTITUTION_MIN = 3;
const DOCTORS_PER_INSTITUTION_MAX = 6;
const NUM_PATIENTS = 100;
const VISITS_PER_PATIENT_MIN = 5;
const VISITS_PER_PATIENT_MAX = 15;
// Zwiększamy zakres dat, żeby historia była dłuższa i ciekawsza
const SCHEDULE_DAYS_BACK = 120; 
const SCHEDULE_DAYS_AHEAD = 60;

const specialisations = [
    'Cardiology', 'Dermatology', 'Neurology', 'Orthopedics', 'Pediatrics',
    'Gynecology', 'Ophthalmology', 'Laryngology', 'Urology', 'Endocrinology'
];

const cities = [
    'New York', 'London', 'Chicago', 'San Francisco', 'Boston', 'Seattle', 'Austin', 'Denver', 'Miami', 'Los Angeles'
];

// --- ASSETS (High Quality) ---
const doctorAssets = {
    scrubs: {
        male: [
            'https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?auto=format&fit=crop&w=400&q=80',
            'https://images.unsplash.com/photo-1537368910025-700350fe46c7?auto=format&fit=crop&w=400&q=80'
        ],
        female: [
            'https://images.unsplash.com/photo-1651008325506-71d34584f343?auto=format&fit=crop&w=400&q=80',
            'https://images.unsplash.com/photo-1594824476967-48c8b964273f?auto=format&fit=crop&w=400&q=80'
        ]
    },
    pediatrics: {
        male: ['https://images.unsplash.com/photo-1622253692010-333f2da6031d?auto=format&fit=crop&w=400&q=80'],
        female: ['https://images.unsplash.com/photo-1559839734-2b71ea197ec2?auto=format&fit=crop&w=400&q=80']
    },
    coat: {
        male: [
            'https://images.unsplash.com/photo-1582752948195-9389e5905619?auto=format&fit=crop&w=400&q=80',
            'https://images.unsplash.com/photo-1622902046580-2b47f47f5471?auto=format&fit=crop&w=400&q=80'
        ],
        female: [
            'https://images.unsplash.com/photo-1594824476967-48c8b964273f?auto=format&fit=crop&w=400&q=80',
            'https://images.unsplash.com/photo-1614346422069-1f478c4075f5?auto=format&fit=crop&w=400&q=80'
        ]
    }
};

const institutionAssets = {
    'Hospital': ['https://images.unsplash.com/photo-1587351021759-3e566b9af923?w=800', 'https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=800'],
    'Clinic': ['https://images.unsplash.com/photo-1516549655169-df83a0674f66?w=800', 'https://images.unsplash.com/photo-1629909613654-28e377c37b09?w=800'],
    'Health Center': ['https://images.unsplash.com/photo-1504813184591-01572f98c85f?w=800']
};

const specStyleMap = {
    'Orthopedics': 'scrubs', 'Urology': 'scrubs', 'Laryngology': 'scrubs', 'Gynecology': 'scrubs', 
    'Pediatrics': 'pediatrics', 'Cardiology': 'coat', 'Dermatology': 'coat', 'Neurology': 'coat', 
    'Ophthalmology': 'coat', 'Endocrinology': 'coat'
};

function getDoctorImage(specialisationName, sex) {
    const style = specStyleMap[specialisationName] || 'coat';
    const pool = doctorAssets[style][sex] || doctorAssets['coat'][sex];
    return faker.helpers.arrayElement(pool);
}

function getInstitutionImage(type) {
    const pool = institutionAssets[type] || institutionAssets['Hospital'];
    return faker.helpers.arrayElement(pool);
}

// --- STANDARD MEDICAL CONTEXT ---
const medicalContext = {
    'Cardiology': {
        symptoms: ['Chest pain', 'High blood pressure', 'Heart palpitations', 'Shortness of breath'],
        diagnoses: ['Hypertension stage 1', 'Arrhythmia detected', 'Stable condition', 'Normal ECG results'],
        recommendations: ['Limit salt intake', 'Regular cardio exercise', 'Prescribed Beta-blockers']
    },
    'Dermatology': {
        symptoms: ['Itchy rash', 'Suspicious mole', 'Dry skin', 'Severe Acne'],
        diagnoses: ['Atopic dermatitis', 'Mole biopsy recommended', 'Acne vulgaris'],
        recommendations: ['Use hypoallergenic cream', 'Avoid sunlight', 'Topical antibiotics']
    },
    'default': {
        symptoms: ['General weakness', 'Headache', 'Routine checkup'],
        diagnoses: ['General fatigue', 'Vitamin D deficiency', 'Good overall health'],
        recommendations: ['Blood tests ordered', 'Rest recommended', 'Healthy diet advised']
    }
};

// --- POWER PATIENT NARRATIVE (The Story of John Doe) ---
// Scenariusze wizyt historycznych (Completed)
const powerPatientHistoryStory = [
    { 
        remarks: "I've been having severe headaches in the morning and feeling dizzy.", 
        note: "Initial consultation. BP 160/100. Diagnosis: Hypertension Stage 2. Prescribed Lisinopril 10mg. Ordered ECG and blood work.",
        title: "Initial Hypertension Diagnosis"
    },
    { 
        remarks: "The cough from the new medicine is annoying, but headaches are better.", 
        note: "Patient reports dry cough (ACE inhibitor side effect). BP 150/95. Switched medication to Losartan 50mg.",
        title: "Medication Adjustment"
    },
    { 
        remarks: "Feeling much better. No side effects.", 
        note: "BP 135/85. Treatment effective. Continuing current dosage. Blood results show elevated cholesterol.",
        title: "Follow-up & Lab Results"
    },
    { 
        remarks: "Chest pain after running for the bus yesterday.", 
        note: "Urgent checkup. ECG performed - Sinus Rhythm. Likely muscular strain but referred for Stress Test to be safe.",
        title: "Chest Pain Investigation"
    },
    { 
        remarks: "Stress test results review.", 
        note: "Stress test negative for ischemia. BP 130/80. Patient advised to improve diet and start light cardio.",
        title: "Test Results Review"
    },
    { 
        remarks: "Routine checkup. Need prescription refill.", 
        note: "BP 128/82. Excellent control. Prescription renewed for 3 months.",
        title: "Routine Control"
    },
    { 
        remarks: "Feeling very thirsty lately and urinating often.", 
        note: "Symptoms suggest hyperglycemia. Finger stick glucose: 200 mg/dL. Ordered HbA1c test immediately.",
        title: "New Symptoms Consultation"
    },
    { 
        remarks: "Here to discuss lab results.", 
        note: "HbA1c 7.5%. Diagnosis: Type 2 Diabetes (Early stage). Metformin 500mg started. Dietitian referral created.",
        title: "Diabetes Diagnosis"
    }
];

// Scenariusze wizyt przyszłych (Upcoming)
const powerPatientFutureStory = [
    { remarks: "Diabetic foot check and blood pressure control.", title: "Combined Checkup" },
    { remarks: "Prescription renewal for Metformin and Losartan.", title: "Prescription Renewal" },
    { remarks: "Consultation regarding diet changes.", title: "Lifestyle Consultation" }
];


function getMedicalData(specialisation) {
    const context = medicalContext[specialisation] || medicalContext['default'];
    return {
        symptom: faker.helpers.arrayElement(context.symptoms),
        diagnosis: faker.helpers.arrayElement(context.diagnoses),
        recommendation: faker.helpers.arrayElement(context.recommendations)
    };
}

async function generateData() {
    try {
        await client.connect();
        console.log('Connected to MongoDB');
        const db = client.db();

        // Cleaning DB
        await db.collection('Specialisation').deleteMany({});
        await db.collection('City').deleteMany({});
        await db.collection('User').deleteMany({});
        await db.collection('Institution').deleteMany({});
        await db.collection('Schedule').deleteMany({});
        await db.collection('Visit').deleteMany({});
        await db.collection('MedicalHistory').deleteMany({});

        const generatedCredentials = [];

        // 1. Basic Data
        const specialisationObjects = specialisations.map(name => ({ _id: new ObjectId(), name, institutionType: false }));
        await db.collection('Specialisation').insertMany(specialisationObjects);
        const cityObjects = cities.map(name => ({ _id: new ObjectId(), name }));
        await db.collection('City').insertMany(cityObjects);
        console.log('✅ Specs & Cities generated');

        // Lists
        const institutions = [];
        const doctors = [];
        const admins = [];
        const schedules = [];
        const patients = [];

        const defaultPasswordHash = await argon2.hash('password123');

        // ---------------------------------------------------------
        // --- POWER USERS SETUP ---
        // ---------------------------------------------------------
        
        // 1. Power Institution (Grand Central Hospital)
        const powerInstId = new ObjectId();
        const powerInstitution = {
            _id: powerInstId,
            name: "Grand Central Hospital",
            address: { province: "New York", number: "101", postalCode: "10001", city: "New York", street: "5th Avenue" },
            isPublic: true, rating: 5.0, types: ["Hospital"], employees: [],
            image: institutionAssets['Hospital'][0],
            description: "The most advanced medical facility in the region.", isActive: true,
            _class: "com.adam.medipathbackend.models.Institution"
        };
        institutions.push(powerInstitution);

        // 2. Power Admin
        const powerAdminId = new ObjectId();
        const powerAdmin = {
            _id: powerAdminId, name: "Admin", surname: "Power", email: "admin@medipath.com", passwordHash: defaultPasswordHash, roleCode: 15, employers: [{ institutionId: powerInstId.toHexString(), institutionName: powerInstitution.name }],
            userSettings: { language: 'en', systemNotifications: true, userNotifications: true, lastPanel: 8 }, isActive: true, notifications: [], pfpimage: "https://ui-avatars.com/api/?name=Admin+Power&background=000&color=fff",
            govId: "00000000000", birthDate: "1980-01-01", phoneNumber: "000-000-000", address: powerInstitution.address, rating: 0, numOfRatings: 0, licenceNumber: "", specialisations: [], latestMedicalHistory: [],
            _class: "com.adam.medipathbackend.models.User"
        };
        admins.push(powerAdmin);
        powerInstitution.employees.push({ userId: powerAdminId.toHexString(), name: "Admin", surname: "Power", roleCode: 9, specialisations: [], pfpimage: powerAdmin.pfpimage });
        generatedCredentials.push({ role: 'Admin (POWER)', email: 'admin@medipath.com' });

        // 3. Power Doctor
        const powerDoctorId = new ObjectId();
        const powerDocSpecs = ['Cardiology', 'Internal Medicine'];
        const powerDoctor = {
            _id: powerDoctorId, name: "Patrick", surname: "Jane", email: "doctor@medipath.com", passwordHash: defaultPasswordHash, roleCode: 3, specialisations: powerDocSpecs,
            rating: 5.0, numOfRatings: 542, employers: [{ institutionId: powerInstId.toHexString(), institutionName: powerInstitution.name }],
            pfpimage:  "https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?auto=format&fit=crop&w=400&q=80",
            userSettings: { language: 'en', systemNotifications: true, userNotifications: true, lastPanel: 2 }, isActive: true, notifications: [],
            govId: "11111111111", birthDate: "1975-03-15", phoneNumber: "555-0199-222", licenceNumber: "9988776", address: { province: "New York", number: "22", postalCode: "10002", city: "New York", street: "Broadway" }, latestMedicalHistory: [],
            _class: "com.adam.medipathbackend.models.User"
        };
        doctors.push(powerDoctor);
        powerInstitution.employees.push({ userId: powerDoctorId.toHexString(), name: "Patrick", surname: "Jane", roleCode: 3, specialisations: powerDocSpecs, pfpimage: powerDoctor.pfpimage });
        generatedCredentials.push({ role: 'Doctor (POWER)', email: 'doctor@medipath.com' });

        // 4. Power Patient
        const powerPatientId = new ObjectId();
        const powerPatient = {
            _id: powerPatientId, name: "John", surname: "Doe", email: "patient@medipath.com", passwordHash: defaultPasswordHash, roleCode: 1,
            userSettings: { language: 'en', systemNotifications: true, userNotifications: true, lastPanel: 1 }, isActive: true, notifications: [],
            pfpimage: "https://ui-avatars.com/api/?name=John+Doe&background=0D8ABC&color=fff",
            govId: "22222222222", birthDate: "1985-07-20", phoneNumber: "555-999-000", address: { province: "New York", number: "50", postalCode: "10005", city: "New York", street: "Wall St" }, rating: 0, numOfRatings: 0, licenceNumber: "", specialisations: [], latestMedicalHistory: [], employers: [],
            _class: "com.adam.medipathbackend.models.User"
        };
        patients.push(powerPatient);
        generatedCredentials.push({ role: 'Patient (POWER)', email: 'patient@medipath.com' });

        // --- SCHEDULE GENERATION ---
        const today = new Date();
        const startDate = new Date(today);
        startDate.setDate(today.getDate() - SCHEDULE_DAYS_BACK);
        const totalDays = SCHEDULE_DAYS_BACK + SCHEDULE_DAYS_AHEAD;

        // Power Doctor Schedule (Full load)
        for (let k = 0; k < totalDays; k++) {
            const currentDate = new Date(startDate);
            currentDate.setDate(startDate.getDate() + k);
            const slotsCount = 10;
            for (let l = 0; l < slotsCount; l++) {
                const slotStart = new Date(currentDate); slotStart.setHours(8 + l, 0, 0, 0); 
                const slotEnd = new Date(slotStart); slotEnd.setMinutes(slotStart.getMinutes() + 30);
                schedules.push({
                    _id: new ObjectId(), startHour: slotStart, endHour: slotEnd,
                    doctor: { userId: powerDoctorId.toHexString(), doctorName: powerDoctor.name, doctorSurname: powerDoctor.surname, specialisations: powerDocSpecs, valid: true },
                    institution: { institutionId: powerInstId.toHexString(), institutionName: powerInstitution.name, valid: true },
                    booked: false, _class: "com.adam.medipathbackend.models.Schedule"
                });
            }
        }

        // Standard Background Generation (Institutions/Docs)
        for (let i = 0; i < NUM_INSTITUTIONS - 1; i++) {
            const institutionId = new ObjectId();
            const instType = faker.helpers.arrayElement(['Hospital', 'Clinic', 'Health Center']);
            const institution = {
                _id: institutionId, name: `${faker.location.city()} ${instType}`, address: { province: faker.location.state(), number: faker.location.buildingNumber(), postalCode: faker.location.zipCode(), city: faker.helpers.arrayElement(cities), street: faker.location.street() },
                isPublic: faker.datatype.boolean(), rating: faker.number.float({ min: 3, max: 4.8, multipleOf: 0.1 }), types: [instType], employees: [], image: getInstitutionImage(instType), description: faker.lorem.paragraph(), isActive: true,
                _class: "com.adam.medipathbackend.models.Institution"
            };
            // ... (skrócona generacja standardowego admina i lekarzy - bez zmian logicznych, tylko kompresja kodu)
            const adminId = new ObjectId();
            const admin = { _id: adminId, name: faker.person.firstName(), surname: faker.person.lastName(), email: faker.internet.email(), passwordHash: defaultPasswordHash, roleCode: 9, employers: [{ institutionId: institutionId.toHexString(), institutionName: institution.name }], userSettings: { language: 'en', systemNotifications: true, userNotifications: true, lastPanel: 9 }, isActive: true, notifications: [], pfpimage: `https://ui-avatars.com/api/?name=Admin+${i}&background=333&color=fff`, govId: faker.string.numeric(11), birthDate: "1990-01-01", phoneNumber: faker.phone.number(), address: institution.address, rating: 0, numOfRatings: 0, licenceNumber: "", specialisations: [], latestMedicalHistory: [], _class: "com.adam.medipathbackend.models.User" };
            admins.push(admin); institution.employees.push({ userId: adminId.toHexString(), name: admin.name, surname: admin.surname, roleCode: 9, specialisations: [], pfpimage: admin.pfpimage });

            const numDoctors = faker.number.int({ min: DOCTORS_PER_INSTITUTION_MIN, max: DOCTORS_PER_INSTITUTION_MAX });
            for (let j = 0; j < numDoctors; j++) {
                const doctorId = new ObjectId();
                const sex = faker.person.sexType(); const name = faker.person.firstName(sex); const surname = faker.person.lastName(sex);
                const doctorSpecs = faker.helpers.arrayElements(specialisations, faker.number.int({ min: 1, max: 2 }));
                const doctor = { _id: doctorId, name, surname, email: faker.internet.email({ firstName: name, lastName: surname }), passwordHash: defaultPasswordHash, roleCode: 3, specialisations: doctorSpecs, rating: faker.number.float({ min: 3.5, max: 5, multipleOf: 0.1 }), numOfRatings: faker.number.int({ min: 5, max: 50 }), employers: [{ institutionId: institutionId.toHexString(), institutionName: institution.name }], pfpimage: getDoctorImage(doctorSpecs[0], sex), userSettings: { language: 'en', systemNotifications: true, userNotifications: true, lastPanel: 3 }, isActive: true, notifications: [], govId: faker.string.numeric(11), birthDate: "1980-05-05", phoneNumber: faker.phone.number(), licenceNumber: faker.string.numeric(7), address: institution.address, latestMedicalHistory: [], _class: "com.adam.medipathbackend.models.User" };
                doctors.push(doctor); institution.employees.push({ userId: doctorId.toHexString(), name: doctor.name, surname: doctor.surname, roleCode: 3, specialisations: doctorSpecs, pfpimage: doctor.pfpimage });
                for (let k = 0; k < totalDays; k += 3) { // Less frequent schedule for std doctors
                    const currentDate = new Date(startDate); currentDate.setDate(startDate.getDate() + k);
                    if (currentDate.getDay() === 0 || currentDate.getDay() === 6) continue;
                    for (let l = 0; l < 4; l++) {
                        const slotStart = new Date(currentDate); slotStart.setHours(10 + l, 0, 0, 0); const slotEnd = new Date(slotStart); slotEnd.setMinutes(slotStart.getMinutes() + 30);
                        schedules.push({ _id: new ObjectId(), startHour: slotStart, endHour: slotEnd, doctor: { userId: doctorId.toHexString(), doctorName: doctor.name, doctorSurname: doctor.surname, specialisations: doctorSpecs, valid: true }, institution: { institutionId: institutionId.toHexString(), institutionName: institution.name, valid: true }, booked: false, _class: "com.adam.medipathbackend.models.Schedule" });
                    }
                }
            }
            institutions.push(institution);
        }

        // Standard Patients
        for (let i = 0; i < NUM_PATIENTS - 1; i++) {
            const sex = faker.person.sexType(); const name = faker.person.firstName(sex); const surname = faker.person.lastName(sex);
            patients.push({ _id: new ObjectId(), name, surname, email: faker.internet.email({ firstName: name, lastName: surname }), passwordHash: defaultPasswordHash, roleCode: 1, userSettings: { language: 'en', systemNotifications: true, userNotifications: true, lastPanel: 1 }, isActive: true, notifications: [], pfpimage: `https://ui-avatars.com/api/?name=${name}+${surname}&background=random&color=fff`, govId: faker.string.numeric(11), birthDate: "1995-05-05", phoneNumber: faker.phone.number(), address: { province: faker.location.state(), number: "1", postalCode: "00000", city: "City", street: "Street" }, rating: 0, numOfRatings: 0, licenceNumber: "", specialisations: [], latestMedicalHistory: [], employers: [], _class: "com.adam.medipathbackend.models.User" });
        }

        await db.collection('Institution').insertMany(institutions);
        await db.collection('User').insertMany([...doctors, ...admins, ...patients]);
        await db.collection('Schedule').insertMany(schedules);
        console.log('✅ Base entities inserted');

        // ---------------------------------------------------------
        // --- VISITS GENERATION ---
        // ---------------------------------------------------------
        
        const visits = [];
        const medicalHistories = [];
        
        let allSchedules = await db.collection('Schedule').find({ booked: false }).toArray();
        const powerDocSchedules = allSchedules.filter(s => s.doctor.userId === powerDoctorId.toHexString());
        let standardSchedules = allSchedules.filter(s => s.doctor.userId !== powerDoctorId.toHexString());

        // --- POWER PATIENT GENERATION (Complex Logic) ---
        // Filter schedules into Past and Future
        const now = new Date();
        const pastPowerSchedules = powerDocSchedules.filter(s => new Date(s.startHour) < now).sort((a, b) => new Date(a.startHour) - new Date(b.startHour));
        const futurePowerSchedules = powerDocSchedules.filter(s => new Date(s.startHour) > now).sort((a, b) => new Date(a.startHour) - new Date(b.startHour));

        // 1. History (Spaced out logic)
        // We want to pick ~8 visits from the past, but spaced by at least 7 days
        let lastVisitDate = new Date('2000-01-01'); // Long ago
        let storyIndex = 0;

        for (const schedule of pastPowerSchedules) {
            const visitDate = new Date(schedule.startHour);
            const daysDiff = (visitDate - lastVisitDate) / (1000 * 60 * 60 * 24);

            // Spaced out by at least 10 days and if we have story left
            if (daysDiff > 10 && storyIndex < powerPatientHistoryStory.length) {
                const story = powerPatientHistoryStory[storyIndex];
                
                const visit = {
                    _id: new ObjectId(),
                    patient: { userId: powerPatientId.toHexString(), name: powerPatient.name, surname: powerPatient.surname, govID: powerPatient.govId, valid: true },
                    doctor: schedule.doctor,
                    time: { scheduleId: schedule._id.toHexString(), startTime: schedule.startHour, endTime: schedule.endHour, valid: true },
                    institution: schedule.institution,
                    status: 'Completed',
                    note: story.note,
                    codes: [{ codeType: 'PRESCRIPTION', code: `${faker.string.numeric(4)}`, isActive: true, _class: "com.adam.medipathbackend.models.Code" },{ codeType: 'REFERRAL', code: `${faker.string.numeric(4)}`, isActive: true, _class: "com.adam.medipathbackend.models.Code" }],
                    patientRemarks: story.remarks,
                    _class: "com.adam.medipathbackend.models.Visit"
                };
                visits.push(visit);
                await db.collection('Schedule').updateOne({ _id: schedule._id }, { $set: { booked: true } });

                medicalHistories.push({
                    _id: new ObjectId(),
                    title: story.title, // Custom meaningful title
                    date: schedule.startHour,
                    note: story.note,
                    userId: powerPatientId.toHexString(),
                    doctor: { userId: schedule.doctor.userId, doctorName: schedule.doctor.doctorName, doctorSurname: schedule.doctor.doctorSurname, specializations: schedule.doctor.specialisations, valid: true },
                    _class: "com.adam.medipathbackend.models.MedicalHistory"
                });

                lastVisitDate = visitDate;
                storyIndex++;
            }
        }

        // 2. Upcoming Visits (Pick 3 distinct future visits)
        let countUpcoming = 0;
        lastVisitDate = new Date(); // Reset for future checks
        for (const schedule of futurePowerSchedules) {
            if (countUpcoming >= 3) break;
            
            const visitDate = new Date(schedule.startHour);
            const daysDiff = (visitDate - lastVisitDate) / (1000 * 60 * 60 * 24);

            if (daysDiff > 7) { // At least a week apart
                const story = powerPatientFutureStory[countUpcoming] || { remarks: "Routine Checkup", title: "Checkup" };
                
                const visit = {
                    _id: new ObjectId(),
                    patient: { userId: powerPatientId.toHexString(), name: powerPatient.name, surname: powerPatient.surname, govID: powerPatient.govId, valid: true },
                    doctor: schedule.doctor,
                    time: { scheduleId: schedule._id.toHexString(), startTime: schedule.startHour, endTime: schedule.endHour, valid: true },
                    institution: schedule.institution,
                    status: 'Upcoming',
                    note: null,
                    codes: [],
                    patientRemarks: story.remarks,
                    _class: "com.adam.medipathbackend.models.Visit"
                };
                visits.push(visit);
                await db.collection('Schedule').updateOne({ _id: schedule._id }, { $set: { booked: true } });

                const notification = {
                    title: `Upcoming: ${story.title}`,
                    content: `Reminder for your visit on ${schedule.startHour.toLocaleDateString()} with Dr. Jane.`,
                    timestamp: new Date(), isSystem: true, isRead: false,
                    _class: "com.adam.medipathbackend.models.Notification"
                };
                await db.collection('User').updateOne({ _id: powerPatientId }, { $push: { notifications: notification } });
                
                lastVisitDate = visitDate;
                countUpcoming++;
            }
        }

        // --- STANDARD PATIENTS GENERATION ---
        standardSchedules = faker.helpers.shuffle(standardSchedules);
        const stdPatients = patients.filter(p => p._id.toHexString() !== powerPatientId.toHexString());

        for (const patient of stdPatients) {
            const numVisits = faker.number.int({ min: VISITS_PER_PATIENT_MIN, max: VISITS_PER_PATIENT_MAX });
            for (let j = 0; j < numVisits; j++) {
                if (standardSchedules.length === 0) break;
                const schedule = standardSchedules.pop();
                const isPast = new Date(schedule.endHour) < new Date();
                const status = isPast ? (faker.datatype.boolean({ probability: 0.8 }) ? 'Completed' : 'Cancelled') : 'Upcoming';
                
                const medInfo = getMedicalData(schedule.doctor.specialisations[0]);
                const visit = {
                    _id: new ObjectId(),
                    patient: { userId: patient._id.toHexString(), name: patient.name, surname: patient.surname, govID: patient.govId, valid: true },
                    doctor: schedule.doctor,
                    time: { scheduleId: schedule._id.toHexString(), startTime: schedule.startHour, endTime: schedule.endHour, valid: true },
                    institution: schedule.institution,
                    status: status,
                    note: status === 'Completed' ? `Diagnosis: ${medInfo.diagnosis}. Plan: ${medInfo.recommendation}` : null,
                    codes: [],
                    patientRemarks: faker.datatype.boolean({ probability: 0.6 }) ? `Patient reports: ${medInfo.symptom}` : "Regular visit.",
                    _class: "com.adam.medipathbackend.models.Visit"
                };
                visits.push(visit);
                await db.collection('Schedule').updateOne({ _id: schedule._id }, { $set: { booked: true } });
                
                if (status === 'Completed') {
                    medicalHistories.push({
                        _id: new ObjectId(), title: `Consultation: ${schedule.doctor.specialisations[0]}`,
                        date: schedule.startHour, note: medInfo.diagnosis, userId: patient._id.toHexString(),
                        doctor: { userId: schedule.doctor.userId, doctorName: schedule.doctor.doctorName, doctorSurname: schedule.doctor.doctorSurname, specializations: schedule.doctor.specialisations, valid: true },
                        _class: "com.adam.medipathbackend.models.MedicalHistory"
                    });
                }
            }
        }

        if (visits.length > 0) await db.collection('Visit').insertMany(visits);
        if (medicalHistories.length > 0) await db.collection('MedicalHistory').insertMany(medicalHistories);

        console.log(`✅ Generated ${visits.length} Visits and ${medicalHistories.length} Medical Histories`);
        
        console.log('\n=============================================');
        console.log('       POWER ACCOUNTS (FULL DATA)           ');
        console.log('=============================================');
        console.log(`ADMIN:   admin@medipath.com   / password123`);
        console.log(`DOCTOR:  doctor@medipath.com  / password123`);
        console.log(`PATIENT: patient@medipath.com / password123`);
        console.log('=============================================');

    } catch (e) {
        console.error(e);
    } finally {
        await client.close();
        console.log('Disconnected from MongoDB');
    }
}

generateData();