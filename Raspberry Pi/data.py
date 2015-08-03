#!/usr/bin/env python

from ABE_ADCPi import ADCPi
from ABE_helpers import ABEHelpers
import serial
import datetime
import time

#initialize i2c code
i2c_helper = ABEHelpers()
bus = i2c_helper.get_smbus()
adc = ADCPi(bus, 0X68, 0X69, 12)

#Delay to let tablet open BT connection
time.sleep(3)

global bluetoothSerial
bluetoothSerial  = serial.Serial( "/dev/rfcomm0", baudrate=9600 )
bluetoothSerial.open()

def sampleAndSend():
  global sampleAndSend
  global i2c_helper
  global bus
  global adc
  global datetime
  
  #sample ADC
  bluetoothSerial.write(str(adc.read_voltage(1)))
  bluetoothSerial.write("_")
  bluetoothSerial.write(str(adc.read_voltage(2)))
  bluetoothSerial.write("_")
  bluetoothSerial.write(str(adc.read_voltage(3)))
  bluetoothSerial.write("_")
  bluetoothSerial.write(str(adc.read_voltage(4)))
  bluetoothSerial.write("_")
  bluetoothSerial.write(str(adc.read_voltage(5)))
  bluetoothSerial.write("\n")

  #sample frequency 1Hz
while True:
	sampleAndSend()
	time.sleep(1)