Electric_Car_Charge_Scheduling_System
=====================================

HIT3138

Scheduling agent

Car charge levels
Time till car is going to be used
Who is currently charging
Transformer power levels
Transformer agent

Holds information
Calculates stuff
Car agent

Schedule
Charge
      T     T
      ^     ^
      |     |
      V     V
C <-> S <-> S <-> C
      ^     ^
      |     |
      V     V
      C     C
Car
- charge : double
- timeTillUse : double
- maxCharge : double
+ getNeededCharge() : double
+ setTimeTillUse()
+ setCharge()
+ isCharged() : boolean
+ chargeCar(chargeRate : double)
Transformer
- limit : double
- powerLevel : double
- output : double
+ getOutput() : double
+ getAvailablePower() : double
+ getLimit() : double
+ charging(chargeRate : double)
Scheduler
- carLimit : int
- numberOfCars : int
- chargeRate : double
+ whoToCharge() : Car
+ setNumberOfCars()
+ isTherePower(neededPower : double) : boolean
+ isThereSlots() : boolean
