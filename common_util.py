import mysql.connector

def do_signup(request_object, cur):
    name = request_object.form.get('name')
    email = request_object.form.get('email')
    phone = request_object.form.get('phone')
    password = request_object.form.get('password')
    user_type = request_object.form.get('role')
    bank = request_object.form.get('bank') if user_type == 'organizer' else None
    try:
        if user_type == 'beneficiary':
            cur.execute(
                "INSERT INTO users (name, email, phone, password) VALUES (%s, %s, %s, %s)",
                (name, email, phone, password)
            )
        elif user_type == 'manager':
            cur.execute(
                "INSERT INTO managers (name, email, phone, password) VALUES (%s, %s, %s, %s)",
                (name, email, phone, password)
            )
        elif user_type == 'organizer':
            cur.execute(
                "INSERT INTO organizers (name, email, phone, password, bank) VALUES (%s, %s, %s, %s, %s)",
                (name, email, phone, password, bank)
            )
        result = cur.fetchall()
        for row in result:
            print(row)
    except mysql.connector.Error as err:
        print(f"Error: {err}")
        
def do_login(request_object, cur):
    email = request_object.form.get('email')
    password = request_object.form.get('password')
    user_type = request_object.form.get('role')
    if user_type == 'user':
        query = "SELECT * FROM users WHERE email = %s AND password = %s"
    elif user_type == 'manager':
        query = "SELECT * FROM managers WHERE email = %s AND password = %s"
    elif user_type == 'organizer':
        query = "SELECT * FROM organizers WHERE email = %s AND password = %s"
    else:
        return False 
    
    cur.execute(query, (email, password))
    
    if cur.fetchone() is not None:
        return True
    else:
        return False



