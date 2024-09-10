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
            return True
        elif user_type == 'organizer':
            cur.execute(
                "INSERT INTO users (name, email, type, phone, password, bank) VALUES (%s, %s, %s, %s, %s, %s)",
                (name, email, user_type, phone, password, bank))
            return True
        else:
            raise ValueError('Invalid user type')
    except Exception as err:
        print(f"Error: {err}")
        return False
        
def do_login(request_object, cur, user_type):
    email = request_object.form.get('email')
    password = request_object.form.get('password')
    print(user_type, "is logging in...")
    if user_type == 'beneficiary' or user_type == 'manager' or user_type == 'organizer':
        query = "SELECT * FROM users WHERE email = %s AND password = %s AND type = %s"
    else:
        return False 
    
    cur.execute(query, (email, password, user_type))
    
    if cur.fetchone() is not None:
        return True
    else:
        return False
    

def get_user_details(email, cur, user_type):
    cur.execute("SELECT * FROM users WHERE email = %s AND type = %s", (email, user_type))
    return cur.fetchone()

def loan_application(request, cur, user_type, user_details):
    name = request.form.get('name')
    dob = request.form.get('dob')
    contact = request.form.get('contact')
    business = request.form.get('business')
    adhaar = request.files.get('adhaar')
    pan = request.files.get('pan')
    passbook = request.files.get('passbook')
    passport = request.files.get('passport')
    email_id = user_details[2]
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

        query = """ INSERT INTO user_loan (email, adhaar_path, pan_path, passbook_path, passport_path)
            VALUES (%s, %s, %s, %s, %s)
        """
        cur.execute(query, (email_id, file_paths['adhaar'], file_paths['pan'], file_paths['passbook'], file_paths['passport']))



