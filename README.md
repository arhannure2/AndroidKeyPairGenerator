# Android Key Pair Generator 
## This project is demo application RSA key-pair on android platform


Screen Shot Notes : 
1. Splash Screen
2. Main Screen 
3. Authentication needed to sign 
4. Signed successful
5. Verified successfully




<p float="left">
  <img src="https://user-images.githubusercontent.com/57435729/145414529-e3ac3d2b-4057-4905-8256-c31a10a8966e.jpg" width="150" height="300">
  <img src="https://user-images.githubusercontent.com/57435729/145414561-1241c9cf-8265-4b20-9ef6-0d8e6a4326bf.jpg" width="150" height="300"> 
  <img src="https://user-images.githubusercontent.com/57435729/145392730-5479647e-005f-47fa-933f-ff8e68d53f82.jpeg" width="150" height="300">
  <img src="https://user-images.githubusercontent.com/57435729/145392738-d1de017f-8793-4fcd-8020-2dfe4fb3ebf9.jpg" width="150" height="300">
  <img src="https://user-images.githubusercontent.com/57435729/145392753-2a88e13b-25a6-4eba-8cc1-caa327d8c2e5.jpg" width="150" height="300">
</p>



In this project, I have explain how to create an RSA key pair on Android and use that key pair for sign and verify data. This RSA key pair will be stored in the Android KeyStore.

# What is RSA?
RSA is a public-key or asymmetric crypto system. It uses a public key for encryption and a private key for decryption. Anyone can use the public key to encrypt a message, but it can be decrypted only by the private key owner.

# What is the Android KeyStore?
The Android KeyStore is a storage facility for cryptographic keys and certificates. The keys stored in the KeyStore can be used for cryptographic operations, but the key material will not be extracted. This means an attacker might use a stored key, but will not be able to export it outside the device. When a key is created from an app and stored in the KeyStore, the access to the key will be restricted to the app itself.

# Why use RSA?
We can use RSA to sign and verify data, for example when we transfer some data to a server. Because RSA is a public-key system, we can use the private key to sign data in our app and send the public key to the server, so the server can verify that the data sent is genuine and hasn’t been tampered with. If any malicious users know the public key, the only thing they can do is verify the integrity of the data, but they cannot change the data because they need the private key to do so.



