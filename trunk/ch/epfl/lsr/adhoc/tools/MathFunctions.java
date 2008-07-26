package ch.epfl.lsr.adhoc.tools;

import java.util.Random;

final class MathFunctions {

        static Random randomGenerator;

        static boolean getUniformDistributedTrigger(float aProbability){

                // lazy initialization
                if (randomGenerator == null){
                        // current system time is seed.
                        randomGenerator = new Random();
                }

                float aFloat;

                aFloat = randomGenerator.nextFloat();

                return (aFloat <= aProbability);
        }

        static float round(float aFloat, int decimalPlaces){
                return (float)(Math.round(aFloat * Math.pow(10, decimalPlaces)) / Math.pow(10, decimalPlaces));
        }
}
