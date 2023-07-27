# This gist contains a direct connection to a local PostgreSQL database
# called "suppliers" where the username and password parameters are "postgres"

# This code is adapted from the tutorial hosted below:
# http://www.postgresqltutorial.com/postgresql-python/connect/

import psycopg2
import json
# Establish a connection to the database by creating a cursor object
# The PostgreSQL server must be accessed through the PostgreSQL APP or Terminal Shell

# conn = psycopg2.connect("dbname=suppliers port=5432 user=postgres password=postgres")


host_val = input("Enter the host: ")
port_val = input("Enter the port: ")
database_val = input("Enter the database name: ")
user_val = input("Enter the user name: ")
password_val = input("Enter the password: ")
#EMAIL_OR_PHONE --?EMAIL
#LACKS_CONFIDENCE-->LACKS_CONFIDENCE_OR_MOTIVATION
#LACKS_MOTIVATION-->LACKS_CONFIDENCE_OR_MOTIVATION
# Or:
#conn = psycopg2.connect(host="localhost", port = 5432, database="education-employment", user="education-employment", password="education-employment")
conn = psycopg2.connect(host=host_val, port = port_val, database=database_val, user=user_val, password=password_val)

sql = """ UPDATE work_readiness
                SET profile_data = %s
                WHERE offender_id = %s"""
deletesql = """ DELETE from work_readiness"""
# Create a cursor object
cur = conn.cursor()

# A sample query of all data from the "vendors" table in the "suppliers" database
cur.execute("""SELECT * FROM work_readiness""")
query_results = cur.fetchall()


for row in query_results:
    offender_id = row[0]
    originaltext = json.dumps(row[7])
    print("offender_id: "+offender_id)
    print("data: "+originaltext)
    print("  \n\n\n\n\n                           ")
#{"status": "READY_TO_WORK", "statusChange": true, "supportAccepted": {"modifiedBy": "GDUTTON_GEN", "workImpacts": {"modifiedBy": "GDUTTON_GEN", "modifiedDateTime": [2023, 2, 15, 15, 25, 34, 910000000], "abilityToWorkImpactedBy": ["EDUCATION_ENROLLMENT"], "ableToManageDependencies": true, "ableToManageMentalHealth": false, "caringResponsibilitiesFullTime": false}, "workInterests": {"modifiedBy": "GDUTTON_GEN", "modifiedDateTime": [2023, 2, 15, 15, 25, 3, 780000000], "workTypesOfInterest": ["HOSPITALITY", "TECHNICAL", "OTHER"], "jobOfParticularInterest": "Some job", "workTypesOfInterestOther": "Some other work"}, "workExperience": {"modifiedBy": "GDUTTON_GEN", "modifiedDateTime": [2023, 2, 15, 15, 25, 3, 780000000], "qualificationsAndTraining": ["DRIVING_LICENSE"], "previousWorkOrVolunteering": "", "qualificationsAndTrainingOther": ""}, "actionsRequired": {"actions": [{"id": null, "status": "COMPLETED", "todoItem": "BANK_ACCOUNT"}, {"id": null, "status": "NOT_STARTED", "todoItem": "EMAIL_OR_PHONE"}, {"id": null, "status": "COMPLETED", "todoItem": "HOUSING"}, {"id": ["DRIVING_LICENCE"], "status": "COMPLETED", "todoItem": "ID"}, {"id": null, "status": "IN_PROGRESS", "todoItem": "CV_AND_COVERING_LETTER"}, {"id": null, "status": "COMPLETED", "todoItem": "DISCLOSURE_LETTER"}], "modifiedBy": "GDUTTON_GEN", "modifiedDateTime": [2023, 2, 15, 15, 26, 16, 253000000]}, "modifiedDateTime": [2023, 2, 15, 15, 27, 33, 197301225]}, "supportDeclined": null, "statusChangeDate": [2023, 2, 15, 15, 27, 33, 197304772], "statusChangeType": "DECLINED_TO_ACCEPTED", "supportAccepted_history": [{"modifiedBy": "GDUTTON_GEN", "workImpacts": {"modifiedBy": "GDUTTON_GEN", "modifiedDateTime": [2023, 2, 15, 15, 25, 3, 780000000], "abilityToWorkImpactedBy": ["EDUCATION_ENROLLMENT", "DEPENDENCY_ISSUES"], "ableToManageDependencies": true, "ableToManageMentalHealth": false, "caringResponsibilitiesFullTime": false}, "workInterests": {"modifiedBy": "GDUTTON_GEN", "modifiedDateTime": [2023, 2, 15, 15, 25, 3, 780000000], "workTypesOfInterest": ["HOSPITALITY", "TECHNICAL", "OTHER"], "jobOfParticularInterest": "Some job", "workTypesOfInterestOther": "Some other work"}, "workExperience": {"modifiedBy": "GDUTTON_GEN", "modifiedDateTime": [2023, 2, 15, 15, 25, 3, 780000000], "qualificationsAndTraining": ["DRIVING_LICENSE"], "previousWorkOrVolunteering": "", "qualificationsAndTrainingOther": ""}, "actionsRequired": {"actions": [{"id": null, "status": "COMPLETED", "todoItem": "BANK_ACCOUNT"}, {"id": null, "status": "NOT_STARTED", "todoItem": "CV_AND_COVERING_LETTER"}, {"id": null, "status": "NOT_STARTED", "todoItem": "DISCLOSURE_LETTER"}, {"id": null, "status": "NOT_STARTED", "todoItem": "EMAIL_OR_PHONE"}, {"id": null, "status": "COMPLETED", "todoItem": "HOUSING"}, {"id": ["DRIVING_LICENCE"], "status": "COMPLETED", "todoItem": "ID"}], "modifiedBy": "GDUTTON_GEN", "modifiedDateTime": [2023, 2, 15, 15, 25, 3, 780000000]}, "modifiedDateTime": [2023, 2, 15, 15, 25, 3, 926461321]}, {"modifiedBy": "GDUTTON_GEN", "workImpacts": {"modifiedBy": "GDUTTON_GEN", "modifiedDateTime": [2023, 2, 15, 15, 25, 34, 910000000], "abilityToWorkImpactedBy": ["EDUCATION_ENROLLMENT"], "ableToManageDependencies": true, "ableToManageMentalHealth": false, "caringResponsibilitiesFullTime": false}, "workInterests": {"modifiedBy": "GDUTTON_GEN", "modifiedDateTime": [2023, 2, 15, 15, 25, 3, 780000000], "workTypesOfInterest": ["HOSPITALITY", "TECHNICAL", "OTHER"], "jobOfParticularInterest": "Some job", "workTypesOfInterestOther": "Some other work"}, "workExperience": {"modifiedBy": "GDUTTON_GEN", "modifiedDateTime": [2023, 2, 15, 15, 25, 3, 780000000], "qualificationsAndTraining": ["DRIVING_LICENSE"], "previousWorkOrVolunteering": "", "qualificationsAndTrainingOther": ""}, "actionsRequired": {"actions": [{"id": null, "status": "COMPLETED", "todoItem": "BANK_ACCOUNT"}, {"id": null, "status": "NOT_STARTED", "todoItem": "CV_AND_COVERING_LETTER"}, {"id": null, "status": "NOT_STARTED", "todoItem": "DISCLOSURE_LETTER"}, {"id": null, "status": "NOT_STARTED", "todoItem": "EMAIL_OR_PHONE"}, {"id": null, "status": "COMPLETED", "todoItem": "HOUSING"}, {"id": ["DRIVING_LICENCE"], "status": "COMPLETED", "todoItem": "ID"}], "modifiedBy": "GDUTTON_GEN", "modifiedDateTime": [2023, 2, 15, 15, 25, 3, 780000000]}, "modifiedDateTime": [2023, 2, 15, 15, 25, 35, 41921786]}, {"modifiedBy": "GDUTTON_GEN", "workImpacts": {"modifiedBy": "GDUTTON_GEN", "modifiedDateTime": [2023, 2, 15, 15, 25, 34, 910000000], "abilityToWorkImpactedBy": ["EDUCATION_ENROLLMENT"], "ableToManageDependencies": true, "ableToManageMentalHealth": false, "caringResponsibilitiesFullTime": false}, "workInterests": {"modifiedBy": "GDUTTON_GEN", "modifiedDateTime": [2023, 2, 15, 15, 25, 3, 780000000], "workTypesOfInterest": ["HOSPITALITY", "TECHNICAL", "OTHER"], "jobOfParticularInterest": "Some job", "workTypesOfInterestOther": "Some other work"}, "workExperience": {"modifiedBy": "GDUTTON_GEN", "modifiedDateTime": [2023, 2, 15, 15, 25, 3, 780000000], "qualificationsAndTraining": ["DRIVING_LICENSE"], "previousWorkOrVolunteering": "", "qualificationsAndTrainingOther": ""}, "actionsRequired": {"actions": [{"id": null, "status": "COMPLETED", "todoItem": "BANK_ACCOUNT"}, {"id": null, "status": "NOT_STARTED", "todoItem": "DISCLOSURE_LETTER"}, {"id": null, "status": "NOT_STARTED", "todoItem": "EMAIL_OR_PHONE"}, {"id": null, "status": "COMPLETED", "todoItem": "HOUSING"}, {"id": ["DRIVING_LICENCE"], "status": "COMPLETED", "todoItem": "ID"}, {"id": null, "status": "IN_PROGRESS", "todoItem": "CV_AND_COVERING_LETTER"}], "modifiedBy": "GDUTTON_GEN", "modifiedDateTime": [2023, 2, 15, 15, 26, 7, 701000000]}, "modifiedDateTime": [2023, 2, 15, 15, 26, 7, 856371721]}, {"modifiedBy": "GDUTTON_GEN", "workImpacts": {"modifiedBy": "GDUTTON_GEN", "modifiedDateTime": [2023, 2, 15, 15, 25, 34, 910000000], "abilityToWorkImpactedBy": ["EDUCATION_ENROLLMENT"], "ableToManageDependencies": true, "ableToManageMentalHealth": false, "caringResponsibilitiesFullTime": false}, "workInterests": {"modifiedBy": "GDUTTON_GEN", "modifiedDateTime": [2023, 2, 15, 15, 25, 3, 780000000], "workTypesOfInterest": ["HOSPITALITY", "TECHNICAL", "OTHER"], "jobOfParticularInterest": "Some job", "workTypesOfInterestOther": "Some other work"}, "workExperience": {"modifiedBy": "GDUTTON_GEN", "modifiedDateTime": [2023, 2, 15, 15, 25, 3, 780000000], "qualificationsAndTraining": ["DRIVING_LICENSE"], "previousWorkOrVolunteering": "", "qualificationsAndTrainingOther": ""}, "actionsRequired": {"actions": [{"id": null, "status": "COMPLETED", "todoItem": "BANK_ACCOUNT"}, {"id": null, "status": "NOT_STARTED", "todoItem": "EMAIL_OR_PHONE"}, {"id": null, "status": "COMPLETED", "todoItem": "HOUSING"}, {"id": ["DRIVING_LICENCE"], "status": "COMPLETED", "todoItem": "ID"}, {"id": null, "status": "IN_PROGRESS", "todoItem": "CV_AND_COVERING_LETTER"}, {"id": null, "status": "COMPLETED", "todoItem": "DISCLOSURE_LETTER"}], "modifiedBy": "GDUTTON_GEN", "modifiedDateTime": [2023, 2, 15, 15, 26, 16, 253000000]}, "modifiedDateTime": [2023, 2, 15, 15, 26, 16, 397030271]}], "supportDeclined_history": [{"modifiedBy": "GDUTTON_GEN", "modifiedDateTime": [2023, 2, 15, 15, 27, 12, 779155018], "supportToWorkDeclinedReason": ["HEALTH"], "supportToWorkDeclinedReasonOther": "", "circumstanceChangesRequiredToWork": ["MENTAL_HEALTH_SUPPORT"], "circumstanceChangesRequiredToWorkOther": ""}]}
    emailorPhoneText_NOT_STARTED = '{"id": null, "status": "NOT_STARTED", "todoItem": "EMAIL_OR_PHONE"}'
    emailAndPhoneText_NOT_STARTED = '{"id": null, "status": "NOT_STARTED", "todoItem": "EMAIL"},{"id": null, "status": "NOT_STARTED", "todoItem": "PHONE"}'
    originaltext=originaltext.replace(emailorPhoneText_NOT_STARTED,emailAndPhoneText_NOT_STARTED)
    emailorPhoneText_IN_PROGRESS = '{"id": null, "status": "IN_PROGRESS", "todoItem": "EMAIL_OR_PHONE"}'
    emailAndPhoneText_IN_PROGRESS = '{"id": null, "status": "IN_PROGRESS", "todoItem": "EMAIL"},{"id": null, "status": "IN_PROGRESS", "todoItem": "PHONE"}'
    originaltext=originaltext.replace(emailorPhoneText_IN_PROGRESS,emailAndPhoneText_IN_PROGRESS)
    emailorPhoneText_COMPLETED = '{"id": null, "status": "COMPLETED", "todoItem": "EMAIL_OR_PHONE"}'
    emailAndPhoneText_COMPLETED = '{"id": null, "status": "COMPLETED", "todoItem": "EMAIL"},{"id": null, "status": "COMPLETED", "todoItem": "PHONE"}'
    originaltext=originaltext.replace(emailorPhoneText_COMPLETED,emailAndPhoneText_COMPLETED)

    lacksMotivationorConfidenceText = '"LACKS_CONFIDENCE_OR_MOTIVATION"'
    lacksMotivationText = '"LACKS_MOTIVATION"'
    originaltext=originaltext.replace(lacksMotivationText,lacksMotivationorConfidenceText)
    lacksConfidenceText = '"LACKS_CONFIDENCE"'
    originaltext=originaltext.replace(lacksConfidenceText,lacksMotivationorConfidenceText)

    cur.execute(sql ,(originaltext,offender_id))
    conn.commit()
# Close the cursor and connection to so the server can allocate
# bandwidth to other requests
cur.close()
conn.close()
