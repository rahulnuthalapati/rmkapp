import mysql.connector
import os

def do_signup(request_object, cur, user_type):
    name = request_object.form.get('name')
    email = request_object.form.get('email')
    phone = request_object.form.get('phone')
    password = request_object.form.get('password')
    bank = request_object.form.get('bank') if user_type == 'organizer' else None
    print(user_type, "is signing up...")
    try:
        if user_type == 'beneficiary' or user_type == 'manager':
            cur.execute(
                "INSERT INTO users (name, email, type, phone, password) VALUES (%s, %s, %s, %s, %s)",
                (name, email, user_type, phone, password))
        elif user_type == 'organizer':
            bank = request_object.form.get('bank')
            if bank not in ['Chennai', 'Tirupati', 'Poonamalle']: # Validate bank selection
                return False 
            cur.execute(
                "INSERT INTO users (name, email, type, phone, password, bank) VALUES (%s, %s, %s, %s, %s, %s)",
                (name, email, user_type, phone, password, bank))
        else:
            raise ValueError('Invalid user type')
        if cur.rowcount > 0:
            print("Signup successful")
            return True
        else:
            print("Signup failed")
            return False
    except Exception as err:
        print(f"Error: {err}")
        return False
    
        
def do_login(request_object, cur, user_type):
    email = request_object.form.get('email')
    password = request_object.form.get('password')
    print(user_type, "with email", email, "and password", password, "is logging in...")
    if user_type == 'beneficiary' or user_type == 'manager' or user_type == 'organizer':
        query = "SELECT * FROM users WHERE email = %s AND password = %s AND type = %s"
    else:
        return False 
    
    cur.execute(query, (email, password, user_type))
    # print(cur.fetchone())

    if cur.fetchone():
        print("Login successful")
        return True
    else:
        print("Login failed")
        return False
    

def get_user_details(email, cur, user_type):
    cur.execute("SELECT * FROM users WHERE email = %s AND type = %s", (email, user_type))
    return cur.fetchone()

def loan_application(request, cur, user_type, user_details):
    name = request.form.get('name')
    dob = request.form.get('dob')
    contact = request.form.get('contact')
    business = request.form.get('business')
    amount = request.form.get('amount')
    loan_status = "pending"
    email_id = user_details[1]

    print(email_id, "is applying for a loan...")

    files = ['adhaar', 'pan', 'passbook', 'passport']
    file_paths = {}

    for file in files:
        file_data = request.files.get(file)
        if file_data:
            new_file_name = f"{email_id}_{file_data.filename}"
            directory = os.path.join('static', 'images', 'loan_applications', email_id)
            if not os.path.exists(directory):
                os.makedirs(directory)
            file_path = os.path.join(directory, new_file_name)
            file_data.save(file_path)
            file_paths[file] = file_path

    # Ensure all paths are available; set missing paths to None
    file_paths = {file: file_paths.get(file, None) for file in files}

    query = """ 
    INSERT INTO user_loan (email, aadhar_path, pan_path, passbook_path, passport_path, loan_amount, loan_status)
    VALUES (%s, %s, %s, %s, %s, %s, %s)
    """
    cur.execute(query, (
        email_id,
        file_paths.get('adhaar'),
        file_paths.get('pan'),
        file_paths.get('passbook'),
        file_paths.get('passport'),
        amount,
        loan_status
    ))

def get_beneficiary_profiles(cur):
    cur.execute('SELECT * FROM users WHERE type = "beneficiary"')
    profiles = cur.fetchall()
    print(profiles)
    return profiles

def get_current_beneficiary_details(cur):
    from main import current_user
    
    user_email = current_user[1]
    
    query = """SELECT * FROM user_business WHERE user_email = %s"""
    cur.execute(query, (user_email))
    result = cur.fetchone()
    return result


def get_all_loans(cur):
    cur.execute('SELECT * FROM user_loan')
    loans = cur.fetchall()
    return loans


def modify_loan_status(cursor, loan_id, loan_status):
    query = "UPDATE user_loan SET loan_status = %s WHERE loan_id = %s"
    cursor.execute(query, (loan_status, loan_id))



