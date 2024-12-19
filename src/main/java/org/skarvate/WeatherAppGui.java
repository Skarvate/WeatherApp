package org.skarvate;

import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class WeatherAppGui extends JFrame {
    private JSONObject weatherData;


    public WeatherAppGui() {
        // set up our gui and add a title
        super("Weather App");

        // configure gui to end the program's process once it has been closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // set the size of our gui (in pixels)
        setSize(450, 650);

        // load our gui at the center of the screen
        setLocationRelativeTo(null);

        // make our layout manager null to manually position our components within gui
        setLayout(null);

        // prevent any resize of our gui
        setResizable(false);

        addGuiComponents();
    }

    private void addGuiComponents() {
        // search field
        JTextField searchTextField = new JTextField();

        // set the location and size of our components
        searchTextField.setBounds(15, 15, 351, 45);

        // change the font style and size
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));

        add(searchTextField);



        // weather image
        JLabel weatherConditionImage = new JLabel(loadImage("src/main/resources/assets/cloudy.png"));
        weatherConditionImage.setBounds(0, 125, 450, 217);
        add(weatherConditionImage);

        // temperature text
        JLabel temperatureText = new JLabel(" 10 C");
        temperatureText.setBounds(0, 350, 450, 54);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 42));

        // center the text
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        // weather condition description
        JLabel weatherConditionDescription = new JLabel("Cloudy");
        weatherConditionDescription.setBounds(0, 405, 450, 36);
        weatherConditionDescription.setFont(new Font("Dialog", Font.PLAIN, 26));
        weatherConditionDescription.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDescription);

        // humidity image
        JLabel humidityImage = new JLabel(loadImage("src/main/resources/assets/humidity.png"));
        humidityImage.setBounds(15, 500, 74, 66);
        add(humidityImage);

        // humidity text
        JLabel humidityText = new JLabel("<html><b>Humidity</b> <br>100%</html>");
        humidityText.setBounds(90, 500, 85, 55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 10));
        add(humidityText);

        // wind speed image
        JLabel windSpeedImage = new JLabel(loadImage("src/main/resources/assets/windspeed.png"));
        windSpeedImage.setBounds(220, 500, 74, 66);
        add(windSpeedImage);

        // wind speed text
        JLabel windSpeedText = new JLabel("<html><b>Windspeed</b> <br>15km/h</html>");
        windSpeedText.setBounds(310, 500, 85, 55);
        windSpeedText.setFont(new Font("Dialog", Font.PLAIN, 10));
        add(windSpeedText);

        // search button
        JButton searchButton = new JButton(loadImage("src/main/resources/assets/search.png"));

        // change the cursor to a hand cursor when hovering over this button
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 47, 45);
        searchButton.addActionListener(e -> {
            // get location from user
            String userInput = searchTextField.getText();

            // validate input - remove whitespace to ensure non-empty text
            if(userInput.replaceAll("\\s", "").length() <= 0){
               return;
            }

            // retrieve weather data
            weatherData = WeatherApp.getWeatherData(userInput);

            // update gui

            // update weather image
            String weatherCondition = (String) Objects.requireNonNull(weatherData).get("weather_condition");

            // depending on the condition, we will update the weather image that corresponds with the condition
            switch(weatherCondition){
                case "Clear":
                    weatherConditionImage.setIcon(loadImage("src/main/resources/assets/clear.png"));
                    break;
                    case "Cloudy":
                    weatherConditionImage.setIcon(loadImage("src/main/resources/assets/cloudy.png"));
                    break;
                    case "Rain":
                    weatherConditionImage.setIcon(loadImage("src/main/resources/assets/rain.png"));
                    break;
                    case "Snow":
                    weatherConditionImage.setIcon(loadImage("src/main/resources/assets/snow.png"));
                    break;
            }

            // update temperature text
            double temperature = (Double) weatherData.get("temperature");
            temperatureText.setText(temperature + "C");

            // update weather condition text
            weatherConditionDescription.setText(weatherCondition);

            // update humidity  text
            long humidty = (Long) weatherData.get("humidity");
            humidityText.setText("<html><b>Humidty</b>" + humidty + "%</html>");

            // update windspeed text
            double windspeed = (Double) weatherData.get("windspeed");
            windSpeedText.setText("<html><b>Windspeed</b>" + windspeed + "km/h</html>");
        });
        add(searchButton);
    }


    // used to create images in our gui components
    private ImageIcon loadImage(String resourcePath) {

        try {
            // read the image file from the path given
            BufferedImage image = ImageIO.read(new File(resourcePath));

            // returns an image icon so that our component can render it
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Could not find resource");
        return null;
    }
}
