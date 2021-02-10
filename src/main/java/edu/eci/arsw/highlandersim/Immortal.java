package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Immortal extends Thread {

	private ImmortalUpdateReportCallback updateCallback = null;

	private AtomicInteger health;

	private int defaultDamageValue;

	private final List<Immortal> immortalsPopulation;

	private final String name;

	private final Random r = new Random(System.currentTimeMillis());

	private boolean pausado;

	private AtomicBoolean atomicBool;

	public Immortal(String name, List<Immortal> immortalsPopulation, AtomicInteger health, int defaultDamageValue,
			ImmortalUpdateReportCallback ucb) {
		super(name);
		this.updateCallback = ucb;
		this.name = name;
		this.immortalsPopulation = immortalsPopulation;
		this.health = health;
		this.defaultDamageValue = defaultDamageValue;
		this.atomicBool = new AtomicBoolean(true);
	}

	public void run() {
		pausado=false;
		while (atomicBool.get()) {
			
			synchronized (this) {
				while (pausado) {
					try {
						this.wait();
					} catch (InterruptedException interruptedException) {
						interruptedException.printStackTrace();
					}
				}
			}
			synchronized (immortalsPopulation) {
				if(this.health.get()==0) {
					atomicBool.getAndSet(false);
				}
				Immortal im;

				int myIndex = immortalsPopulation.indexOf(this);

				int nextFighterIndex = r.nextInt(immortalsPopulation.size());

				// avoid self-fight
				if (nextFighterIndex == myIndex) {
					nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
				}
				
				im = immortalsPopulation.get(nextFighterIndex);
				this.fight(im);
				
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

	public synchronized void fight(Immortal i2) {
		if (i2.getHealth().get() > 0 && atomicBool.get()) {
			i2.changeHealth( new AtomicInteger(i2.getHealth().get() - defaultDamageValue));
			this.health.addAndGet(defaultDamageValue);
			updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
		} else {
			
			updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
			i2.pausar();
		}

	}

	public synchronized void changeHealth(AtomicInteger v) {
		health = v;
	}

	public AtomicInteger getHealth() {
		return health;
	}

	@Override
	public String toString() {

		return name + "[" + health + "]";
	}

	public synchronized void pausar() {
		pausado = true;
	}

	public synchronized void reaunudar() {
		pausado = false;
		notifyAll();
	}
	
	public void setBool(boolean bool){
        this.atomicBool = new AtomicBoolean(bool);
    }

}
