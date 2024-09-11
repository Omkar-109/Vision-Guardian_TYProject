# Vision Guardian

**Vision Guardian** is a groundbreaking mobile application designed to enhance the independence and safety of visually impaired individuals. By harnessing the power of cutting-edge technology, the app provides:

- **Comprehensive navigation support**
- **Real-time object detection**
- **Access to vital information**

All of this is controlled through **intuitive voice commands**, making the app entirely hands-free and user-friendly.

---

## Key Features

### 1. Voice Assistant – **NOVA**
The app features a voice assistant named **NOVA** that responds to user prompts with human-like interactions using the ChatGPT API. Key functions include:
- Speech synthesis
- Location services
- Weather updates
- Interaction with other app functions

### 2. Object Detection
The app uses the **SSDMobileNetTensorflowLite** model to detect the following objects:

| **Detected Objects** | | | |
|----------------------------------|----------------------------------|----------------------------------|----------------------------------|
| Airplane                         | Bench                            | Bicycle                          | Bird                            |
| Boat                             | Bus                              | Car                              | Cat                             |
| Fire Hydrant                     | Motorcycle                       | Parking Meter                    | Person                          |
| Stop Sign                        | Traffic Light                    | Train                            | Truck                           |
| Elephant                         | Frisbee                          | Horse                            | Sheep                           |
| Bear                             | Cow                              | Dog                              | Zebra                           |
| Giraffe                          | Skateboard                       | Bowl                             | Kite                            |
| Baseball Bat                     | Surfboard                        | Snowboard                        | Sports Ball                     |
| Tennis Racket                    | Hot Dog                          | Toilet                           | Orange                          |
| Banana                           | Apple                            | Broccoli                         | Carrot                          |
| Sandwich                         | Pizza                            | Donut                            | Cake                            |
| Microwave                        | Teddy Bear                       | Remote                           | TV                              |
| Hair Dryer                       | Wine Glass                       | Chair                            | Refrigerator                    |
| Oven                             | Keyboard                         | Mouse                            | Cell Phone                      |
| Bed                              | Vase                             | Couch                            | Dining Table                    |



### 3. Obstacle Avoidance
The app helps visually impaired users avoid obstacles using an **ultrasonic sensor** connected via **Bluetooth** to an **ESP32** sensor. This provides real-time feedback on nearby objects, ensuring safer navigation.

---

## Hardware Setup

The hardware setup consists of:
- **ESP32 Sensor**: For communication via Bluetooth.
- **Ultrasonic Sensor (HC-SR04)**: For detecting obstacles.
- **Battery and TP4056 Module**: To power the device.

The hardware is encased in a **3D-printed, lightweight, and durable casing**, ensuring users can move freely and confidently.

---

## Technology Stack

- **Object Detection**: SSDMobileNet TensorFlow Lite
- **Voice Assistant**: ChatGPT API (NOVA)
- **Bluetooth Communication**: ESP32 Microcontroller with Ultrasonic Sensor (HC-SR04)
- **Programming Languages**: Java, Python
- **Platform**: Android

---

## How It Works

1. **Obstacle Avoidance**: The ultrasonic sensor detects obstacles and sends the data to the app via Bluetooth. 
2. **Object Detection**: TensorFlow Lite identifies objects in the environment.
3. **Voice Assistance**: NOVA provides verbal feedback on object detection, location, and general queries.

<br>

## **Overall Working Flow**

![wholeWorking](https://github.com/user-attachments/assets/ada44e5f-d82f-4c52-8bea-04cc0b058313)

<br>

## **Working of Hardware Circuitry**

![espWorking](https://github.com/user-attachments/assets/dfd0a8b5-1d46-495c-9502-56d1423d9e74)

---

## References

1. [ESP32 Documentation](https://espressif-docs.readthedocs-hosted.com/projects/arduino-esp32/en/latest/)
2. [HC-SR04 Ultrasonic Sensor Manual](https://web.eece.maine.edu/~zhu/book/lab/HC-SR04%20User%20Manual.pdf)
3. [Kaggle TensorFlow Models](https://www.kaggle.com/models/tensorflow/ssd-mobilenet-v1/frameworks/tfLite/variations/metadata/versions/2?tfhub-redirect=true)
4. [Integrate OpenAI GPT-3.5 in Android](https://programmerworld.co/android/how-to-integrate-open-ai-chat-gpt-model-gpt-3-5-turbo-in-your-android-app/)
5. [Create Personal Voice Assistant for Android](https://programmerworld.co/android/how-to-create-a-personal-voice-assistant-android-app-to-create-a-text-file-complete-source-code/)
6. [TensorFlow Lite Object Detection](https://www.tensorflow.org/lite/android/tutorials/object_detection)
7. [TP4056 Module (YouTube)](https://youtu.be/iA0KqqUVQCs?feature=shared)

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
