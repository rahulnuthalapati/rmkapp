from flask import Flask, request, redirect, url_for, render_template, jsonify, session
import mysql.connector
import databaseop as dbop
import common_util as cu
from datetime import datetime
import os
import json
from werkzeug.utils import secure_filename
import requests


cnx = mysql.connector.connect(user='root', password='')

cursor = cnx.cursor()
dbop.checkdb(cursor)

app = Flask(__name__, static_folder='static')
app.secret_key = 'Your_Very_Long_Random_Secret_Key_Here'

# current_role = None
# current_user = None


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
    url = 'https://your-ngrok-url.ngrok-free.app'  # Replace with your ngrok URL
    headers = {
        'ngrok-skip-browser-warning': 'true'
    }

    response = requests.get(url, headers=headers)

    # print(response.text)
    return render_template('index.html')

@app.route('/home')
def home_page():
    url = 'https://your-ngrok-url.ngrok-free.app'  # Replace with your ngrok URL
    headers = {
        'ngrok-skip-browser-warning': 'true'
    }

    response = requests.get(url, headers=headers)

    print(response.text)
    return render_template('index.html')

@app.route('/login', methods=['GET', 'POST'])
def login_page():
    if request.form.get('role') is not None:
        session['role'] = request.form.get('role')
    return render_template('login.html', role=session.get('role'))

@app.route('/api/login', methods=['POST'])
def login():
    current_role = session.get('role')
    if current_role is None:
        return "Please select role from homepage, redirecting in 3 seconds...", 401
    # global current_user
    success = cu.do_login(request, cursor, current_role)
    if not success:
       return "Invalid credentials, please try again.", 401
    session['user'] = cu.get_user_details(request.form.get('email'), cursor, current_role)
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
    return render_template('signup.html', role=session.get('role'))

@app.route('/api/signup', methods=['POST'])
def signup():
    # global current_role
    current_role = session.get('role')
    if current_role is None:
        return "Please select role from homepage", 401
    print("signup for", request.form.get('email'), "is in progress...")
    success = cu.do_signup(request, cursor, current_role)
    if not success:
       return "Error occured while creating account, check logs in server.", 520
    cnx.commit()
    print("signup for", request.form.get('email'), "is successful")
    return url_for('login_page'), 200

@app.route('/beneficiary', methods=['GET', 'POST'])
def beneficiary_page():
    # global current_user
    # global current_role
    current_user = session.get('user')
    current_role = session.get('role')
    if current_user is None or current_role != 'beneficiary':
        current_role = 'beneficiary'
        return redirect(url_for('login_page'))
    return render_template('beneficiary.html')


@app.route('/beneficiary_link', methods=['GET'])
def beneficiary_link_page():
    # email = request.args.get('email')  # Get the email from the query parameters
    #
    # # Fetch the beneficiary data using the email (you'll replace this with your actual logic)
    # beneficiary_data = get_beneficiary_data_by_email(email)

    # if beneficiary_data is None:
    #     return "Beneficiary not found", 404

    # Pass the fetched data to the beneficiary.html template to display it
    return render_template('beneficiary_link.html')


@app.route('/manager')
def manager_page():
    # global current_user
    # global current_role
    current_role = session.get('role')
    current_user = session.get('user')
    if current_user is None or current_role != 'manager':
        current_role = 'manager'
        return redirect(url_for('login_page'))
    return render_template('manager.html')

@app.route('/organizer')
def organizer_page():
    # global current_user
    # global current_role
    current_role = session.get('role')
    current_user = session.get('user')
    if current_user is None or current_role != 'organizer':
        current_role = 'organizer'
        return redirect(url_for('login_page'))
    return render_template('organizer.html')

@app.route('/loan_application_page')
def loan_application_page():
    return render_template('loan_application.html')

@app.route('/api/loanapplication', methods=['POST'])
def loan_application():
    current_role = session.get('role')
    current_user = session.get('user')
    cu.loan_application(request, cursor, current_role, current_user)
    cnx.commit()
    return jsonify({'message': 'Loan application submitted, redirecting in 3 seconds...', 'redirect': '/beneficiary'}), 200


@app.route('/api/user_loan', methods=['GET'])
def get_user_loan_requests():
    # global current_user
    current_role = session.get('role')
    current_user = session.get('user')
    if current_user is None:
        return jsonify({'error': 'No current user'}), 400
    email = current_user[1] if current_role == 'beneficiary' else request.args.get('email')
    print("fetching loan details for user", email)
    try:
        # Fetch loan requests for the current user
        # Assuming current_user[1] contains the email
        loan_id = request.args.get('loanId') 
        print("fetching loan details for user", email, "with loan id", loan_id)

        if loan_id: 
            query = """
            SELECT * FROM user_loan WHERE email = %s AND loan_id = %s 
            """
            cursor.execute(query, (email, loan_id))
        else: 
            query = """
            SELECT * FROM user_loan WHERE email = %s
            """
            cursor.execute(query, (email,))
        
        result = cursor.fetchall()

        # print("Result: ", result)

        if not result:
            return jsonify({"loan_requests_exists": False})

        loan_requests = []
        for row in result:
            loan_requests.append({
                'loan_id': row[0],
                'email': row[1],
                'loan_amount': row[6],
                'loan_status': row[7],
                'aadhar_path': row[2], 
                'pan_path': row[3],
                'passbook_path': row[4],
                'passport_path': row[5],
                'profilePicPath': row[2] if row[3] else '/static/images/webpages/blank_profile.webp'  # Default image if path is None
            })

        print(jsonify({
            "loan_requests_exists": True,
            "loans": loan_requests
        }))

        return jsonify({
            "loan_requests_exists": True,
            "loans": loan_requests
        })

    except Exception as e:
        print(f"Error fetching user loan requests: {e}")
        return jsonify({"error": "Error fetching user loan requests"}), 500

@app.route('/profile_page')
def profile_page():
    return render_template('profile.html')

@app.route('/api/profileinfo', methods=['GET'])
def profile_info(): 
    # global current_user
    current_user = session.get('user')
    print("Current user: ", current_user)
    email = request.args.get('email') 

    if email:  # If email is provided in query parameters, fetch that user's profile
        try:
            cursor.execute("SELECT * FROM users WHERE email = %s", (email,))
            user_details = cursor.fetchone()

            if not user_details:
                return jsonify({'error': 'User not found'}), 404

            name = user_details[0]
            profile_picture = user_details[9] if user_details[9] is not None else '/static/images/webpages/blank_profile.webp'

            user_info = {
                "name": name,
                "profilePicPath": profile_picture
            }

            return jsonify(user_info)
        except Exception as e:
            print(f"Error fetching user details: {e}")
            return jsonify({'error': 'Error fetching user details'}), 500
    else:  # Otherwise, fetch the current user's profile
        profile_picture = current_user[9] if current_user[9] is not None else '/static/images/webpages/blank_profile.webp'
        dob_str = current_user[5]
        dob_datetime = datetime.strptime(current_user[5], '%a, %d %b %Y %H:%M:%S %Z') if current_user[5] else None
        user_info = { "name": current_user[0], "phone": current_user[3], "dob": dob_datetime.strftime('%Y-%m-%d') if dob_datetime else None, "aadharno": current_user[6], "bankaccno": current_user[7], "profilePicPath": profile_picture}
        return jsonify(user_info)

@app.route('/api/get_user_role', methods=['GET'])
def get_user_role():
    # global current_role 
    current_role = session.get('role')
    if current_role == 'organizer': 
        return jsonify({'is_manager': True}), 200
    else:
        return jsonify({'is_manager': False}), 200

@app.route('/api/update_profile', methods=['POST'])
def update_profile():
    # global current_user
    current_user = session.get('user')
    current_role = session.get('role')
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

@app.route('/loan_payment_page')
def loan_payment_page():
    return render_template('loan_payments.html')

@app.route('/api/loan_information', methods=['GET'])
def api_loan_information():
    # global current_user
    current_user = session.get('user')
    current_role = session.get('role')
    if current_user is None:
        return jsonify({'error': 'No current user'}), 400
    email = None
    if request.args.get('email') is None or current_role == 'beneficiary':
        email = current_user[1]
    else:
        email = request.args.get('email')
    print("Email is: ", email)
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

            payment_query = """
            SELECT 1 FROM loan_payments WHERE loan_id = %s LIMIT 1
            """
            cursor.execute(payment_query, (loan_id,))
            payment_exists = bool(cursor.fetchone())
            # print("Payment exists: ", payment_exists)   
        
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
    # global current_user
    current_user = session.get('user')
    email = request.args.get('email') or current_user[1]
    print("Email is: ", request.args.get('email'), email)
    if request.method == 'GET':
        try:
            cursor.execute("SELECT * FROM user_business WHERE email = %s", (email,))
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
        except Exception as e:
            return jsonify({'success': False, 'error': str(e)}), 500

    elif request.method == 'POST':
        data = request.get_json()
        try:
            cursor.execute("SELECT 1 FROM user_business WHERE email = %s", (email,))
            exists = cursor.fetchone()

            if exists:
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
                    data['profit_margin'], email
                ))
            else:
                cursor.execute("""
                    INSERT INTO user_business (type, name, address, monthly_revenue, annual_revenue, 
                        monthly_expense, annual_expense, profit_margin, email)
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
                """, (
                    data['type'], data['name'], data['address'], data['monthly_revenue'],
                    data['annual_revenue'], data['monthly_expense'], data['annual_expense'],
                    data['profit_margin'], email
                ))

            cnx.commit()
            return jsonify({'success': True}), 200

        except Exception as e:
            cnx.rollback()
            return jsonify({'success': False, 'error': str(e)}), 500

@app.route('/income_savings_page')
def income_savings_page():
    return render_template('income_savings.html')

@app.route('/api/income_savings', methods=['GET', 'POST'])
def income_savings():
    # global current_user
    current_user = session.get('user')
    current_role = session.get('role')
    if current_user is None:
        return jsonify({'error': 'User not logged in'}), 401
    
    email = None
    if request.args.get('email') is None or current_role == 'beneficiary':
        email = current_user[1]
    else:
        email = request.args.get('email')
    if request.method == 'GET':
        try:
            cursor.execute("SELECT * FROM user_income_savings WHERE email = %s", (email,))
            data = cursor.fetchone()
            
            if data:
                return jsonify({
                    'success': True,
                    'monthly_income': data[1], 
                    'annual_income': data[2], 
                    'sources': data[3], 
                    'savings_balance': data[4], 
                    'savings_acc_details': data[5], 
                    'monthly_savings_contributions': data[6]
                })
            else:
                return jsonify({'success': True})  

        except Exception as e:
            print(f"Error fetching income and savings data: {e}")
            return jsonify({'success': False, 'error': 'Error fetching data'}), 500

    elif request.method == 'POST':
        try:
            data = request.get_json()

            cursor.execute("SELECT 1 FROM user_income_savings WHERE email = %s", (email,))
            exists = cursor.fetchone()

            if exists:
                update_query = """
                UPDATE user_income_savings SET 
                    monthly_income = %s,
                    annual_income = %s,
                    sources = %s,
                    savings_balance = %s,
                    savings_acc_details = %s,
                    monthly_savings_contributions = %s
                WHERE email = %s
                """
                cursor.execute(update_query, (
                    data['monthly_income'], data['annual_income'], data['sources'], 
                    data['savings_balance'], data['savings_acc_details'], 
                    data['monthly_savings_contributions'], email
                ))
            else:
                # Insert new data
                insert_query = """
                INSERT INTO user_income_savings (
                    email, monthly_income, annual_income, sources, savings_balance, 
                    savings_acc_details, monthly_savings_contributions
                ) VALUES (%s, %s, %s, %s, %s, %s, %s)
                """
                cursor.execute(insert_query, (
                    email, data['monthly_income'], data['annual_income'], data['sources'], 
                    data['savings_balance'], data['savings_acc_details'], 
                    data['monthly_savings_contributions']
                ))
            
            cnx.commit()
            return jsonify({'success': True, 'message': 'Income and savings data saved successfully'}), 200

        except Exception as e:
            print(f"Error saving income and savings data: {e}")
            cnx.rollback()
            return jsonify({'success': False, 'error': 'Error saving data'}), 500

@app.route('/feedback_page')
def feedback_page():
    return render_template('feedback.html')

@app.route('/api/feedback', methods=['GET', 'POST'])
def api_feedback():
    # global current_user
    current_user = session.get('user')
    if current_user is None:
        return jsonify({'error': 'User not logged in'}), 401
    
    email = current_user[1]

    if request.method == 'GET':
            try:
                cursor.execute("SELECT feedback_id, feedback_date, feedback_text FROM feedback WHERE email = %s", (email,)) 
                feedbacks = cursor.fetchall()
                feedback_data = []
                for feedback in feedbacks:
                    feedback_data.append({
                        "feedback_id": feedback[0],
                        "feedback_date": feedback[1].strftime('%Y-%m-%d') if feedback[1] else None,  # Format the date
                        "feedback_text": feedback[2],
                        "feedback_summary": feedback[2][:50] + "..." if len(feedback[2]) > 50 else feedback[2]
                    })
                return jsonify(feedback_data), 200

            except Exception as e:
                print(f"Error fetching feedback: {e}")
                return jsonify({'error': 'Error fetching feedback'}), 500

    elif request.method == 'POST':
        try:
            data = request.get_json()
            feedback_text = data.get('feedback_text')

            if not feedback_text:
                return jsonify({'error': 'Feedback text is required'}), 400

            # Get current date
            current_date = datetime.now().strftime('%Y-%m-%d')  

            cursor.execute("""
            INSERT INTO feedback (email, feedback_date, feedback_text) VALUES (%s, %s, %s)
            """, (email, current_date, feedback_text)) 
            cnx.commit()

            return jsonify({'success': True, 'message': 'Feedback submitted successfully'}), 200

        except Exception as e:
            print(f"Error submitting feedback: {e}")
            cnx.rollback()
            return jsonify({'error': 'Error submitting feedback'}), 500
        

@app.route('/notifications_page')
def notifications_page():
    return render_template('notifications.html')

@app.route('/loan_requests')
def loan_requests():
    return render_template('loan_requests.html')

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

@app.route('/api/modify_loan_status', methods=['POST'])
def modify_loan_status():
    data = request.get_json()
    loan_id = data.get('loan_id')
    loan_status = data.get('loan_status')

    print("Request: ", data)

    if not loan_id or not loan_status:
        return jsonify({'error': 'Loan ID and loan status are required'}), 400

    try:
        cu.modify_loan_status(cursor, loan_id, loan_status)
        cnx.commit()
        return jsonify({'success': True, 'message': 'Loan status modified successfully'}), 200
    except Exception as e:
        print(f"Error modifying loan status: {e}")
        return jsonify({'error': 'Error modifying loan status'}), 500

@app.route('/api/get_all_loans', methods=['GET'])
def get_all_loans():
    try:
        result = cu.get_all_loans(cursor)

        print("Result: ", result)
        if result is None:
            print(jsonify({"loan_requests_exists": False}))
            return jsonify({"loan_requests_exists": False})

        loan_requests = []
        for row in result:
            loan_requests.append({
                'loan_id': row[0],
                'email': row[1],
                'loan_amount': row[6],
                'loan_status': row[7],
                'profilePicPath': row[2] if row[3] else '/static/images/webpages/blank_profile.webp'  
            })

        print("Loan Requests: ", loan_requests)

        print(jsonify({
            "loan_requests_exists": True,
            "loans": loan_requests
        }))

        return jsonify({
            "loan_requests_exists": True,
            "loans": loan_requests
        })

    except Exception as e:
        print(f"Error fetching user loan requests: {e}")
        return jsonify({"error": "Error fetching user loan requests"}), 500

@app.route('/loan_details_page')
def loan_details_page():
    return render_template('loan_details.html') 

@app.route('/api/logout', methods=['GET'])
def logout():
    # global current_user
    # current_user = None
    # global current_role
    # current_role = None
    session.clear()
    return redirect('/home')

@app.route('/record_payment_page', methods=['GET']) 
def record_payment_page(): 
    return render_template('record_payment.html')

@app.route('/api/record_payment', methods=['POST'])
def record_payment():
    try:
        loan_id = request.form.get('loan_id')
        email = request.form.get('email')
        amount = request.form.get('amount')
        payment_date = request.form.get('payment_date')

        if not all([loan_id, email, amount, payment_date]):
            return jsonify({'error': 'Missing required data'}), 400

        # Insert payment details into the database
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

@app.route('/system_settings')
def system_settings():
    return render_template('system_settings.html')

@app.route('/edit_profile_page')
def edit_profile_page():
    return render_template('edit_profile_page.html')

@app.route('/change_password_page')
def change_password_page():
    return render_template('change_password_page.html')

@app.route('/api/profile', methods=['GET', 'POST'])
def profile_api():
    # global current_user
    current_user = session.get('user')
    if request.method == 'GET':
        try:
            cursor.execute("SELECT * FROM users WHERE email = %s", (current_user[1],))
            user_data = cursor.fetchone()

            if user_data:
                return jsonify({
                    'name': user_data[0],
                    'bank': user_data[8],
                    'address': user_data[10]
                }), 200
            else:
                return jsonify({'error': 'User not found'}), 404
        except Exception as e:
            return jsonify({'error': str(e)}), 500
    elif request.method == 'POST':
        try:
            name = request.form.get('name')
            bank = request.form.get('bank')
            address = request.form.get('address')

            # Update user data in the database
            cursor.execute("""
                UPDATE users
                SET name = %s, bank = %s, address = %s
                WHERE email = %s
            """, (name, bank, address, current_user[1]))
            cnx.commit()

            return jsonify({'success': True}), 200
        except Exception as e:
            print(f"Error updating user profile: {e}")
            return jsonify({'success': False, 'error': str(e)}), 500

@app.route('/api/change_password', methods=['POST'])
def change_password_api():
    # global current_user
    current_user = session.get('user')
    data = request.get_json()
    current_password = data.get('current_password')
    new_password = data.get('new_password')

    if not current_password or not new_password:
        return jsonify({'success': False, 'error': 'Missing current or new password'}), 400

    try:
        # Check if the current password matches
        cursor.execute("SELECT password FROM users WHERE email = %s", (current_user[1],))
        actual_password = cursor.fetchone()

        if not actual_password or actual_password[0] != current_password:
            return jsonify({'success': False, 'error': 'Incorrect current password'}), 401

        # Update the password
        cursor.execute("UPDATE users SET password = %s WHERE email = %s", (new_password, current_user[1]))
        cnx.commit()

        return jsonify({'success': True}), 200
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)}), 500

@app.route('/chat_page')
def chat_page():
    # global current_user
    # global current_role
    current_user = session.get('user')
    current_role = session.get('role')
    if current_user is None:
        return redirect(url_for('home_page'))  # Redirect if not logged in
    return render_template('chat.html', current_user=current_user, current_role=current_role)

@app.route('/api/get_users_for_chat', methods=['GET'])
def get_users_for_chat():
    # global current_user
    # global current_role
    current_user = session.get('user')
    current_role = session.get('role')
    if current_user is None:
        return jsonify({'error': 'User not logged in'}), 401

    try:
        if current_role == 'organizer':
            cursor.execute("SELECT email, name, profile_pic FROM users WHERE type = 'beneficiary'")
        elif current_role == 'beneficiary':
            cursor.execute("SELECT email, name, profile_pic FROM users WHERE type = 'organizer'")
        else:  # current_role is'manager' or any other unexpected role
            return jsonify({'error': 'Chat is not available for this user role'}), 403 # Forbidden
 
        users = []
        all_rows = cursor.fetchall() # Fetch all rows first

        for row in all_rows:
            profile_pic = row[2] if row[2] is not None else '/static/images/webpages/blank_profile.webp'
            users.append({'email': row[0], 'name': row[1], 'profilePicPath': profile_pic, 'has_unread_messages': False})

        if current_user:
            cursor.execute("""
                SELECT DISTINCT sender_email 
                FROM chat_messages
                WHERE receiver_email = %s AND is_read = FALSE
            """, (current_user[1],))
            senders_with_unread_messages = [row[0] for row in cursor.fetchall()]

            # Update users with unread messages
            for user in users:
                if user['email'] in senders_with_unread_messages:
                    user['has_unread_messages'] = True
        
        print(users)
        return jsonify(users), 200

    except Exception as e:
        print(f"Error fetching users for chat: {e}")
        return jsonify({'error': 'Failed to fetch users'}), 500

@app.route('/api/send_message', methods=['POST'])
def send_message():
    # global current_user
    current_user = session.get('user')
    if current_user is None:
        return jsonify({'error': 'User not logged in'}), 401

    data = request.get_json()
    receiver_email = data.get('receiver_email')
    message_text = data.get('message_text')

    if not receiver_email or not message_text:
        return jsonify({'error': 'Missing receiver email or message text'}), 400

    try:
        cursor.execute("""
            INSERT INTO chat_messages (sender_email, receiver_email, message_text, is_read)
            VALUES (%s, %s, %s, %s)
        """, (current_user[1], receiver_email, message_text, False))
        cnx.commit()
        return jsonify({'success': True}), 200
    except Exception as e:
        print(f"Error sending message: {e}")
        return jsonify({'error': 'Failed to send message'}), 500

@app.route('/api/get_messages/<receiver_email>', methods=['GET'])
def get_messages(receiver_email):
    # global current_user
    current_user = session.get('user')
    if current_user is None:
        return jsonify({'error': 'User not logged in'}), 401
    try:
        temp = cursor.fetchall()  # Or cursor.fetchone() if you expect only one row
        # Get messages between the current user and the specified receiver
        cursor.execute("""
            SELECT sender_email, message_text, timestamp 
            FROM chat_messages
            WHERE (sender_email = %s AND receiver_email = %s)
               OR (sender_email = %s AND receiver_email = %s)
            ORDER BY timestamp
        """, (current_user[1], receiver_email, receiver_email, current_user[1]))

        messages = [{'sender': row[0], 'message': row[1], 'timestamp': row[2]} for row in cursor.fetchall()]

        # print(messages)
        return jsonify(messages), 200
    except Exception as e:
        print(f"Error fetching messages: {e}")
        return jsonify({'error': 'Failed to fetch messages'}), 500

@app.route('/api/mark_messages_read/<sender_email>', methods=['POST'])
def mark_messages_read(sender_email):
    # global current_user
    current_user = session.get('user')
    if current_user is None:
        return jsonify({'error': 'User not logged in'}), 401

    try:
        new_connection = mysql.connector.connect(user='root', password='')
        # new_cursor = new_connection.cursor()
        with new_connection.cursor() as new_cursor:
            new_cursor.execute("USE rmkdb")
            # Now execute the UPDATE query
            new_cursor.execute("""
                UPDATE chat_messages
                SET is_read = TRUE
                WHERE sender_email = %s AND receiver_email = %s AND is_read = FALSE
            """, (sender_email, current_user[1]))
            new_connection.commit()
        return jsonify({'success': True}), 200
    except Exception as e:
        print(f"Error marking messages as read: {e}")
        return jsonify({'error': 'Failed to mark messages as read'}), 500

@app.route('/api/check_unread_messages/<sender_email>', methods=['GET'])
def check_unread_messages(sender_email):
    # global current_user
    current_user = session.get('user')
    if current_user is None:
        return jsonify({'error': 'User not logged in'}), 401

    try:
        new_connection = mysql.connector.connect(user='root', password='')
        # new_cursor = new_connection.cursor()
        with new_connection.cursor() as new_cursor:
            new_cursor.execute("USE rmkdb")
            new_cursor.execute("""
                SELECT 1 FROM chat_messages
                WHERE sender_email = %s AND receiver_email = %s AND is_read = FALSE
            """, (sender_email, current_user[1]))
            has_unread_messages = bool(new_cursor.fetchone())
        return jsonify({'has_unread_messages': has_unread_messages}), 200
    except Exception as e:
        print(f"Error checking for unread messages: {e}")
        return jsonify({'error': 'Failed to check unread messages'}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, threaded=True)