package GUI

import javafx.animation.AnimationTimer

//This class calls function given as a parameter repeatedly.
class Ticker(function: () => Unit) extends AnimationTimer {
    private var lastUpdate: Long = 0
    //Override from animation timer
    override def handle(now: Long): Unit = {
        if (now - lastUpdate >= 10_000_000) {
            function()
            lastUpdate = now
        }
    }

}
