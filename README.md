# AXA-Trustmile-corda

<img src="documentation/trustmile-it-works.gif" width="400">

At our AXA hackaton 2019 we wanted to show and proof that mileage records can be stored securely, unchangedable into a blockchain using corda DLT: called "trusted miles".
Insurance companies could use this "3rd party trust" to use it as a proof in the policy and therefor allow certain discounts or not.
At the same time insurance companies could detect fraud of "turning back mileage" using the ledger.

## Showcase

how can we showcase this?

- we used a carrera track with 2 speed cars where all the visitors could drive around
- we detected every lap of the 2 tracks using Arduino ESP32 with ultrasound detectors, Led signals with C++
- at every lap we simulated 1000 km and pushed them via [Azure IOT hub, using MQTT](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-mqtt-support) to not loose any message
- every message was then received, transformed and sent to CORDA nodes per car
- simple UI using jquery and websockets to display accurate mileage information from CORDA

how can we show that it is secured?
- we have implemented a "HACK" button attached to the arduino
- every "HACK" button pressed, the mileage had been divided by 2 sent to the ledger. The ledger detected a fraud and kept the mileage
- insurance knows that the mileage was hacked

Showcase
 
[<img src="documentation/video-screen1.png" width="200">](https://youtu.be/z67Uf4xcazc)

[<img src="documentation/video-screen2.png" width="200">](https://youtu.be/QTSV1NbKYiM)

# Technologies used


# Some pictures 

Picture | Description
------------ | -------------
<img src="documentation/technologies-used.jpg" width="400"> | Technologies use
<img src="documentation/hackathon-version.jpg" width="200"> | Arduino board Hack version
<img src="documentation/hackathon-version-3d-print.jpg" width="200"> | During the hack and at a presentaiton 1 week later even using a 3D printed version<br>order it here at [ThingiVerse](https://www.thingiverse.com/thing:3880234)<br>by [Salvador Richter](https://www.thingiverse.com/salvador-richter)
<img src="documentation/trustmile-milestone.jpg" width="200"> | Successful 1st mileage stored in CORDA. Thx Manu, Francesco, Lolo

Demo | Demo
------------ | -------------
<img src="documentation/demo-trustmile-1.jpg" width="300"> | <img src="documentation/demo-trustmile-3.jpg" width="250">
<img src="documentation/demo-trustmile-2.jpg" width="300"> | <img src="documentation/trust-mile-certificate.jpg" width="300">



# Team
## Foto
![Hackteam](documentation/trustmile_teamfoto_small.jpg)

thx to the great team: Sonja, Bernhard, Manu, Sky, Lolo, Salvador, Francesco




