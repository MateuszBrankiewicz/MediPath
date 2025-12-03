#!/bin/sh

# Wait for MongoDB to be ready
until mongosh --host mongodb -u root -p secret --authenticationDatabase admin --eval "db.adminCommand('ping')"; do
  echo "Waiting for MongoDB to be ready..."
  sleep 2
done

# Check if the User collection has documents
USER_COUNT=$(mongosh --host mongodb -u root -p secret --authenticationDatabase admin --eval "db.getSiblingDB('medipath').User.countDocuments()" --quiet)

if [ "$USER_COUNT" -eq 0 ]; then
  echo "No data found in the database. Seeding data..."
  node generate-data.js
else
  echo "Database already contains data. Skipping seeding."
fi
