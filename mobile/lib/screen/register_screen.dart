import 'package:flutter/material.dart';
import 'package:mobile/components/custom_text_field.dart';

class RegisterScreen extends StatefulWidget {
  const RegisterScreen({super.key});

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  bool _isChecked = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(30),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: Image.asset('images/logo.png', width: 90, height: 90),
            ),
            SizedBox(height: 60),
            SizedBox(
              width: 250,
              child: Text(
                'Take care of your health.',
                style: TextStyle(fontSize: 26, fontWeight: FontWeight.bold),
              ),
            ),
            Text(
              'Create an account',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w500,
                color: Color(0xFF284662),
              ),
            ),
            SizedBox(height: 30),
            SizedBox(
              height: 550,
              child: Column(
                mainAxisAlignment: MainAxisAlignment.spaceAround,
                children: [
                  CustomTextField(hintText: 'Name'),
                  CustomTextField(hintText: 'Surname'),
                  CustomTextField(hintText: 'Government ID'),
                  CustomTextField(hintText: 'Birth Date (DD-MM-YYYY)'),
                  Row(
                    children: [
                      Expanded(child: CustomTextField(hintText: 'Number')),
                      SizedBox(width: 12),
                      Expanded(child: CustomTextField(hintText: 'Street')),
                    ],
                  ),
                  CustomTextField(hintText: 'Email Address'),
                  CustomTextField(hintText: 'Password'),
                  CustomTextField(hintText: 'Confirm password'),
                ],
              ),
            ),
            CheckboxListTile(
              value: _isChecked,
              onChanged: (value) {
                setState(() {
                  _isChecked = value ?? false;
                });
              },
              controlAffinity: ListTileControlAffinity.leading,
              contentPadding: EdgeInsets.zero,
              title: Text(
                "Accept Terms & Conditions",
                style: TextStyle(fontSize: 13, fontWeight: FontWeight.bold),
              ),
            ),
            SizedBox(
              width: 400,
              child: ElevatedButton(
                onPressed: () {},
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.black,
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(30),
                  ),
                  padding: EdgeInsets.symmetric(horizontal: 24, vertical: 14),
                ),
                child: Text('SIGN UP'),
              ),
            ),
            Row(
              children: [
                Text(
                  'Already have an account? ',
                  style: TextStyle(fontWeight: FontWeight.w400),
                ),
                Text('Sign in', style: TextStyle(fontWeight: FontWeight.bold)),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
