from flask import Flask, request, redirect, url_for, render_template, jsonify
import mysql.connector
import databaseop as dbop
import common_util as cu

cnx = mysql.connector.connect(user='root', password='')

cursor = cnx.cursor()
dbop.checkdb(cursor)

app = Flask(__name__)

current_role = None
current_user = None

@app.route('/')
def home_page():
    return render_template('index.html')

@app.route('/login', methods=['GET', 'POST'])
def login_page():
    global current_role
    if request.form.get('role') is not None:
        current_role = request.form.get('role')
    return render_template('login.html', role=current_role)

@app.route('/api/login', methods=['POST'])
def login():
    global current_user
    success = cu.do_login(request, cursor, current_role)
    if not success:
       return "Invalid credentials, please try again.", 401
    current_user = cu.get_user_details(request.form.get('email'), cursor, current_role)
    if current_role == 'beneficiary':
        return url_for('beneficiary_page')
    elif current_role == 'manager':
        return url_for('manager_page')
    elif current_role == 'organizer':
        return url_for('organizer_page')
    else:
        return "Invalid role", 401

@app.route('/signup')
def signup_page():
    return render_template('signup.html', role=current_role)

@app.route('/api/signup', methods=['POST'])
def signup():
    success = cu.do_signup(request, cursor, current_role)
    if not success:
       return "Error occured while creating account, check logs in server.", 520
    cnx.commit()
    return url_for('login_page'), 200

@app.route('/beneficiary', methods=['GET', 'POST'])
def beneficiary_page():
    return render_template('beneficiary.html')

@app.route('/manager')
def manager_page():
    return render_template('manager.html')

@app.route('/organizer')
def organizer_page():
    return render_template('organizer.html')

@app.route('/loan_application_page')
def loan_application_page():
    return render_template('loan_application.html')

@app.route('/profile_page')
def profile_page():
    return render_template('profile.html')

@app.route('/api/profileinfo', methods=['GET'])
def profile_info():  
    user_info = { "name": current_user[0], "email": current_user[1], "type": current_user[2], "phone": current_user[3],
        "password": current_user[4], "dob": current_user[5], "aadhar": current_user[6], "bank": current_user[7] }
    print(user_info)
    return jsonify(user_info)

@app.route('/api/update_profile', methods=['POST'])
def update_profile():
    name = request.form.get('name')
    phone = request.form.get('phone')
    password = request.form.get('password')
    dob = request.form.get('dob')
    aadhar = request.form.get('aadhar')
    bank = request.form.get('bank')
    cursor.execute("UPDATE users SET name = %s, phone = %s, password = %s, dob = %s, aadharno = %s, bank = %s WHERE email = %s AND type = %s",
        (name, phone, password, dob, aadhar, bank, current_user[1], current_user[2]))
    cnx.commit()
    return "Profile updated successfully", 200

@app.route('/loan_information_page')
def loan_information_page():
    return render_template('loan_information.html')

@app.route('/economic_activities_page')
def economic_activities_page():
    return render_template('economic_activities.html')

@app.route('/income_savings_page')
def income_savings_page():
    return render_template('income_savings.html')

@app.route('/feedback_page')
def feedback_page():
    return render_template('feedback.html')

@app.route('/notifications_page')
def notifications_page():
    return render_template('notifications.html')

if __name__ == '__main__':
    app.run(debug=True)