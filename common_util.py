import mysql.connector

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



