package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private static final int MIN = 0;
    private static final int MAX = 100;
    private static final int ATTEMPTS = 10;

    private DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        //Pezzo nuovo
        final InputStream fileStream =  ClassLoader.getSystemResourceAsStream("config.yml");
        Configuration configuration;
        Configuration.Builder configurationBuilder = new Configuration.Builder();
        try(final BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                StringTokenizer tokenizer = new StringTokenizer(line, ": ");
                String configurationName = (String)tokenizer.nextElement();
                int configurationValue = Integer.parseInt((String)tokenizer.nextElement());
                System.out.println(configurationValue);
                if (configurationName.equals("minimum")) {
                    configurationBuilder.setMin(configurationValue);
                } else if(configurationName.equals("maximum")) {
                    configurationBuilder.setMax(configurationValue);
                } else {
                    configurationBuilder.setAttempts(configurationValue);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        configuration = configurationBuilder.build();
        
        if(configuration.isConsistent()) {
            this.model = new DrawNumberImpl(configuration);
        }
        //this.model = new DrawNumberImpl(MIN, MAX, ATTEMPTS);    
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl());
    }

}