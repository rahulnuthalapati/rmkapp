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

# @app.before_request
# def filter():
#     if current_role is None and current_user is None:
#         if request.path == '/login' or request.path == '/api/login' or request.endpoint == 'login_page' or request.endpoint == 'login': 
#             return
#         if current_role is None:
#             return redirect('home')
#     if current_user is None and current_role is not None:
#         return redirect('login')
        
@app.route('/')
def index():
    return render_template('index.html')

@app.route('/home')
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
    global current_role
    if current_role is None:
        return "Please select role from homepage, redirecting in 3 seconds...", 401
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
    print("signup for", request.form.get('email'), "is in progress...")
    success = cu.do_signup(request, cursor, current_role)
    if not success:
       return "Error occured while creating account, check logs in server.", 520
    cnx.commit()
    print("signup for", request.form.get('email'), "is successful")
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
    return jsonify({'message': 'Loan application submitted, redirecting in 3 seconds...', 'redirect': '/beneficiary'}), 200
    

@app.route('/profile_page')
def profile_page():
    return render_template('profile.html')

@app.route('/api/profileinfo', methods=['GET'])
def profile_info(): 
    profile_picture = current_user[9] if current_user[9] is not None else '/static/images/webpages/blank_profile.webp'
    user_info = { "name": current_user[0], "phone": current_user[3], "dob": current_user[5].strftime('%Y-%m-%d') if current_user[5] else None, "aadharno": current_user[6], "bankaccno": current_user[7], "profilePicPath": profile_picture}
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

@app.route('/api/loan_information', methods=['GET'])
def api_loan_information():
    global current_user
    if current_user is None:
        return jsonify({'error': 'No current user'}), 400
    email = current_user[1]
    try:
        query = """
        SELECT * FROM user_loan
        WHERE email = %s
        """
        cursor.execute(query, (email,))
        loan_infos = cursor.fetchall()
        print(loan_infos)

        if not loan_infos:
            return jsonify({"loan_exists": False})

        loan_data = []
        for loan_info in loan_infos:
            loan_id = loan_info[0]

            # Check if payments exist for this loan_id
            payment_query = """
            SELECT 1 FROM loan_payments WHERE loan_id = %s LIMIT 1
            """
            cursor.execute(payment_query, (loan_id,))
            payment_exists = bool(cursor.fetchone())
        
            loan_data.append({
                 "loan_id": loan_info[0],                  # Assuming loan_id is the first column
                 "loan_amount": loan_info[6],
                 "loan_status": loan_info[7],
                 "payment_exists": payment_exists})
        print(loan_data)
        return jsonify({
                "loan_exists": True,
                "loans": loan_data
            })

    except Exception as e:
        print(f"Error fetching loan information: {e}")
        return jsonify({"error": "Error fetching loan information"}), 500
    
@app.route('/api/record_payment', methods=['POST'])
def record_payment():
    try:
        data = request.get_json()
        loan_id = data.get('loan_id')
        email = data.get('email')
        amount = data.get('amount')
        payment_date = data.get('payment_date')

        if not all([loan_id, email, amount, payment_date]):
            return jsonify({'error': 'Missing required data'}), 400

        query = """
        INSERT INTO loan_payments (loan_id, email, amount, payment_date)
        VALUES (%s, %s, %s, %s)
        """
        cursor.execute(query, (loan_id, email, amount, payment_date))
        cnx.commit()
        return jsonify({'message': 'Payment recorded successfully'}), 200
    except Exception as e:
        print(f"Error recording payment: {e}")
        cnx.rollback()
        return jsonify({'error': 'Error recording payment'}), 500


@app.route('/api/get_loan_payments', methods=['GET'])
def get_loan_payments():
    try:
        loan_id = request.args.get('loan_id')
        if not loan_id:
            return jsonify({'error': 'Loan ID is required'}), 400
        query = """
        SELECT * FROM loan_payments
        WHERE loan_id = %s
        """
        cursor.execute(query, (loan_id,))
        payments = cursor.fetchall()
        payment_data = []
        for payment in payments:
            payment_data.append({
                "payment_id": payment[0],
                "amount": payment[3],
                "payment_date": payment[4].strftime('%Y-%m-%d') if payment[4] else None 
            })
        return jsonify(payment_data), 200
    except Exception as e:
        print(f"Error fetching loan payments: {e}")
        return jsonify({'error': 'Error fetching loan payments'}), 500    

@app.route('/economic_activities')
def economic_activities_page():
    return render_template('economic_activities.html')

@app.route('/api/economic_activity', methods=['GET', 'POST'])
def manage_economic_activity():
    if request.method == 'GET':
        cursor.execute("SELECT * FROM user_business WHERE email = %s", (current_user[1], ))  # Replace with actual business name logic
        result = cursor.fetchone()
        if result is None:
            return jsonify({'message': 'No data found for this user. Please add data first.'}), 200
        return jsonify({
                'type': result[3],
                'name': result[1],
                'address': result[2],
                'monthly_revenue': result[4],
                'annual_revenue': result[5],
                'monthly_expense': result[6],
                'annual_expense': result[7],
                'profit_margin': result[8]
            })

    elif request.method == 'POST':
        data = request.get_json()
        try:
            cursor.execute("SELECT * FROM user_business WHERE email = %s", (current_user[1],))
            result = cursor.fetchone()

            if result:
                cursor.execute("""
                    UPDATE user_business SET
                        type = %s,
                        name = %s,
                        address = %s,
                        monthly_revenue = %s,
                        annual_revenue = %s,
                        monthly_expense = %s,
                        annual_expense = %s,
                        profit_margin = %s
                    WHERE email = %s
                """, (
                    data['type'], data['name'], data['address'], data['monthly_revenue'], 
                    data['annual_revenue'], data['monthly_expense'], data['annual_expense'], 
                    data['profit_margin'], current_user[1]
                ))
            else:
                cursor.execute("""
                    INSERT INTO user_business (type, name, address, monthly_revenue, annual_revenue, 
                        monthly_expense, annual_expense, profit_margin, email)
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
                """, (
                    data['type'], data['name'], data['address'], data['monthly_revenue'], 
                    data['annual_revenue'], data['monthly_expense'], data['annual_expense'], 
                    data['profit_margin'], current_user[1]
                ))

            cnx.commit()
            return jsonify({'success': True})

        except Exception as e:
            cnx.rollback()
            return jsonify({'success': False, 'error': str(e)})

    if request.method == 'GET':
        # Fetch business details from the `user_business` table
        cursor.execute("SELECT * FROM user_business WHERE name = %s", ('Business Name',))  # Replace with actual business name logic
        result = cursor.fetchone()
        if result:
            return jsonify({
                'type': result['type'],
                'name': result['name'],
                'address': result['address'],
                'monthly_revenue': result['monthly_revenue'],
                'annual_revenue': result['annual_revenue'],
                'monthly_expense': result['monthly_expense'],
                'annual_expense': result['annual_expense'],
                'profit_margin': result['profit_margin']
            })
        else:
            return jsonify({'error': 'No business found'}), 404

    elif request.method == 'POST':
        data = request.get_json()
        try:
            cursor.execute("""
                UPDATE user_business SET
                    type = %s,
                    name = %s,
                    address = %s,
                    monthly_revenue = %s,
                    annual_revenue = %s,
                    monthly_expense = %s,
                    annual_expense = %s,
                    profit_margin = %s
                WHERE email = %s
            """, (
                data['type'], data['name'], data['address'], data['monthly_revenue'], 
                data['annual_revenue'], data['monthly_expense'], data['annual_expense'], 
                data['profit_margin'], current_user[1]
            ))
            cnx.commit()
            return jsonify({'success': True})
        except Exception as e:
            cnx.rollback()
            return jsonify({'success': False, 'error': str(e)})

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

@app.route('/api/beneficiary_profiles', methods=['GET'])
def beneficiary_profiles():
    profiles = cu.get_beneficiary_profiles(cursor)
    
    profiles_for_json = []
    for profile in profiles:
        dob = profile[5]

        if isinstance(dob, datetime):
            dob = dob.strftime('%Y-%m-%d') if dob else None
        elif isinstance(dob, int):
            dob = datetime.fromtimestamp(dob).strftime('%Y-%m-%d') if dob else None
        else:
            dob = None

        profiles_for_json.append({
            "name": profile[0],
            "email": profile[1],
            "phone": profile[3],
            "dob": dob,
            "aadharno": profile[6],
            "bankaccno": profile[7],
            "profilePicPath": profile[9] if profile[9] is not None else '/static/images/webpages/blank_profile.webp'
        })
        print(profile[9])

    return jsonify(profiles_for_json)

@app.route('/loan_management_page')
def loan_management_page():
    return render_template('loan_management.html')

@app.route('/api/logout', methods=['GET'])
def logout():
    global current_user
    current_user = None
    global current_role
    current_role = None
    return redirect('/home')

if __name__ == '__main__':
    app.run(debug=True)