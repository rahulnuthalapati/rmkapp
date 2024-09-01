from flask import Flask, request, redirect, url_for, render_template
import mysql.connector
import databaseop as dbop
import common_util as cu

cnx = mysql.connector.connect(user='root', password='')

cursor = cnx.cursor()
dbop.checkdb(cursor)

app = Flask(__name__)

current_role = None

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
    success = cu.do_login(request, cursor)
    if not success:
       return "Invalid credentials, please try again.", 401
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
    cu.do_signup(request, cursor)
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

@app.route('/loan-information')
def loan_information_page():
    return render_template('loan_information.html')

@app.route('/economic-activities')
def economic_activities_page():
    return render_template('economic_activities.html')

if __name__ == '__main__':
    app.run(debug=True)