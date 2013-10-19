Electric_Car_Charge_Scheduling_System
=====================================

HIT3138

This project will use JADE to provide the functionality for the project

For this project we will have a decentralised system where cars will communicate with other car agents in order to get a charging order.

There will be a GUI to allow for controlling the Agents inputs as will as a GUI to view a summary of the slot values.

Later on there will be a way of swapping algorithms and a dynamic power transformer to provide some extra constraints.

<BR>

CarGui
* Input TimeTillUse
* Input TimeNeededTill charge
* Click Store to store values 
* Click update to send messages to other car agents. Other car agents will then assign slot values depending on times of the other cars

SummaryGui
* Click update to ask cars for values. 
* Summary will then sort these values and output them.

TransformerGui
* Click update to check if car can be added to queue and if it can send a message to cae agent to start charging
* Random charge button to create a random value for the energyLimit

