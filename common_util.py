def do_signup(request_object, cur):
    name = request_object.json.get('name')
    email = request_object.json.get('email')
    phone = request_object.json.get('phone')
    password = request_object.args.get('password')
    bank = request_object.json.get('bank')
    if(request_object.json.get('bank') is not None):
        bank = request_object.json.get('bank')
    
    type = request_object.json.get('type')
    if type == 'user':
        cur.execute("INSERT INTO users (name, email, phone, password) VALUES (%s, %s, %s, %s)", (name, email, phone, password))
    elif type == 'manager':
        cur.execute("INSERT INTO managers (name, email, phone, password) VALUES (%s, %s, %s, %s)", (name, email, phone, password))
    elif type == 'organizer':
        cur.execute("INSERT INTO organizers (name, email, phone, password, bank) VALUES (%s, %s, %s, %s, %s)", (name, email, phone, password, bank))
    
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



