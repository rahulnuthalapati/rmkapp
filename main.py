from flask import Flask, request, redirect, url_for, render_template, jsonify
import mysql.connector
import databaseop as dbop
import common_util as cu
from datetime import datetime
import os
import json
from werkzeug.utils import secure_filename

cnx = mysql.connector.connect(user='root', password='')

cursor = cnx.cursor()
dbop.checkdb(cursor)

app = Flask(__name__, static_folder='static')

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
    global current_user
    global current_role
    if current_user is None or current_role != 'beneficiary':
        current_role = 'beneficiary'
        return redirect(url_for('login_page'))
    return render_template('beneficiary.html')

@app.route('/manager')
def manager_page():
    global current_user
    global current_role
    if current_user is None or current_role != 'manager':
        current_role = 'manager'
        return redirect(url_for('login_page'))
    return render_template('manager.html')

@app.route('/organizer')
def organizer_page():
    global current_user
    global current_role
    if current_user is None or current_role != 'organizer':
        current_role = 'organizer'
        return redirect(url_for('login_page'))
    return render_template('organizer.html')

@app.route('/loan_application_page')
def loan_application_page():
    return render_template('loan_application.html')

@app.route('/api/loanapplication', methods=['POST'])
def loan_application():
    cu.loan_application(request, cursor, current_role, current_user)
    cnx.commit()
    return "success", 200
    

@app.route('/profile_page')
def profile_page():
    return render_template('profile.html')

@app.route('/api/profileinfo', methods=['GET'])
def profile_info(): 
    user_info = { "name": current_user[0], "phone": current_user[3], "dob": current_user[5].strftime('%Y-%m-%d') if current_user[5] else None, "aadharno": current_user[6], "bankaccno": current_user[7], "profilePicPath": current_user[9] }
    return jsonify(user_info)

@app.route('/api/update_profile', methods=['POST'])
def update_profile():
    global current_user
    try:
        data = json.loads(request.form.get('profileData'))
        name = data.get('name')
        phone = data.get('phone')
        dob = data.get('dob')
        aadharno = data.get('aadharno')
        bankno = data.get('bankaccno')
        profile_pic = request.files.get('profilePic')
        query = "UPDATE users SET "
        params = []
        fields_updated = False
        print("data: ", data)

        if name:
            query += "name = %s, "
            params.append(name)
            fields_updated = True
        if phone:
            query += "phone = %s, "
            params.append(phone)
            fields_updated = True
        if dob:
            query += "dob = %s, "
            params.append(dob)
            fields_updated = True
        if aadharno:
            query += "aadharno = %s, "
            params.append(aadharno)
            fields_updated = True
        if bankno:
            query += "bankaccno = %s, "
            params.append(bankno)
            fields_updated = True
        if profile_pic:
            UPLOAD_FOLDER = 'static/images/profile_pictures'

            if not os.path.exists(UPLOAD_FOLDER):
                os.makedirs(UPLOAD_FOLDER)

            filename = secure_filename(f"profile_{(current_user[1] + current_user[0]).replace('.', '')}.png")
            filepath = os.path.join(UPLOAD_FOLDER, filename)
            profile_pic.save(filepath)
            query += "profile_pic = %s, "
            params.append(filepath)
            fields_updated = True

        if not fields_updated:
            return jsonify({"success": True, "message": "No changes were made"}), 200
        # Remove the last comma and add the WHERE clause
        query = query[:-2] + " WHERE email = %s"
        params.append(current_user[1])
        print("query: ", query, "params: ", params)
        cursor.execute(query, tuple(params))
        cnx.commit()

        if current_user is not None:
            current_user = cu.get_user_details(current_user[1], cursor, current_role)

        return jsonify({"success": True, "message": "Profile updated successfully"}), 200
    except Exception as e:
        print(e)
        return jsonify({"success": False, "message": "Error updating profile"}), 500

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

@app.route('/loan_requests')
def loan_requests():
    return render_template('loan_requests.html')

@app.route('/system_settings')
def system_settings():
    return render_template('system_settings.html')

@app.route('/beneficiary_profiles_page')
def beneficiary_profiles_page():
    return render_template('beneficiary_profiles.html')

@app.route('/loan_management_page')
def loan_management_page():
    return render_template('loan_management.html')

if __name__ == '__main__':
    app.run(debug=True)