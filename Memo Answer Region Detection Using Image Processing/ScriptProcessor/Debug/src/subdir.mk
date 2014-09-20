################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../src/BlobDetector.cpp \
../src/DisplayImage.cpp \
../src/HorizontalLineDetection.cpp \
../src/LineDetectorDriver.cpp \
../src/Main.cpp 

OBJS += \
./src/BlobDetector.o \
./src/DisplayImage.o \
./src/HorizontalLineDetection.o \
./src/LineDetectorDriver.o \
./src/Main.o 

CPP_DEPS += \
./src/BlobDetector.d \
./src/DisplayImage.d \
./src/HorizontalLineDetection.d \
./src/LineDetectorDriver.d \
./src/Main.d 


# Each subdirectory must supply rules for building sources it contributes
src/%.o: ../src/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -I/usr/local/include/opencv -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


