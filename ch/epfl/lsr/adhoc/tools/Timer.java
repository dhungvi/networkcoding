package ch.epfl.lsr.adhoc.tools;

/**
 * @author Yoav
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Timer {

        private long startTime, stopTime;

        public Timer(){
                reset();
        }

        void reset(){
                startTime = 0;
                stopTime = 0;
        }

        private String calcHMS(long timeInMilliseconds) {
                long hours, minutes, seconds, timeInSeconds;

                timeInSeconds = timeInMilliseconds / 1000;

                hours = timeInSeconds / 3600;
                timeInSeconds = timeInSeconds - (hours * 3600);
                minutes = timeInSeconds / 60;
                timeInSeconds = timeInSeconds - (minutes * 60);
                seconds = timeInSeconds;
                return new String(hours + " hour(s) " + minutes + " minute(s) " + seconds + " second(s)");
        }

        void start(){
                startTime = System.currentTimeMillis();
        }

        void stop(){
                stopTime = System.currentTimeMillis();
        }

        String getElapsedTime(){
                return calcHMS(stopTime - startTime);
        }


}
