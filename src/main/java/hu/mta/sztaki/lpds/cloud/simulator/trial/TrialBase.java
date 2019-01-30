package hu.mta.sztaki.lpds.cloud.simulator.trial;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.energy.MonitorConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.examples.jobhistoryprocessor.DCFJob;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.util.PowerTransitionGenerator;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.mta.sztaki.lpds.cloud.simulator.examples.vmstat.VMStatParser;
import hu.mta.sztaki.lpds.cloud.simulator.helpers.job.Job;
import hu.mta.sztaki.lpds.cloud.simulator.helpers.job.JobListAnalyser;
import hu.mta.sztaki.lpds.cloud.simulator.helpers.trace.TraceManagementException;

//Inspired by hu.mta.sztaki.lpds.cloud.simulator.examples.SingleVMOverloader

public class TrialBase {
	static List<Job> jobber;
	static int jobhits = 0;
	static List<Job> jobs;
	static int jobslen;
	static long basetime;
	static String dir = System.getProperty("user.dir") + "/src/main/java/hu/mta/sztaki/lpds/cloud/simulator/trial/vmsstat.txt";;
	
	//Inspired by https://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java
	public static int countLines(String filename) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(filename));
	    try {
	        byte[] c = new byte[1024];

	        int readChars = is.read(c);
	        if (readChars == -1) {
	            // bail out if nothing to read
	            return 0;
	        }

	        // make it easy for the optimizer to tune this loop
	        int count = 0;
	        while (readChars == 1024) {
	            for (int i=0; i<1024;) {
	                if (c[i++] == '\n') {
	                    ++count;
	                }
	            }
	            readChars = is.read(c);
	        }

	        // count remaining characters
	        while (readChars != -1) {
	            System.out.println(readChars);
	            for (int i=0; i<readChars; ++i) {
	                if (c[i] == '\n') {
	                    ++count;
	                }
	            }
	            readChars = is.read(c);
	        }

	        return count == 0 ? 1 : count;
	    } finally {
	        is.close();
	    }
	}

	public static List<Job> tryJobs() {

		
		try

		{

			VMStatParser parser = new VMStatParser(
					dir, 0, countLines(dir), true,
					DCFJob.class);

			jobber = parser.getAllJobs();

		} catch (NoSuchMethodException e)

		{
			e.getCause();
		}

		catch (SecurityException e) {
			e.getCause();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jobber;
	}

	public static void loadTraceUntoVM() throws VMManagementException, NetworkException, SecurityException,
			InstantiationException, IllegalAccessException, NoSuchFieldException {

		Timed.resetTimed();

		int machinecores = 1;
		System.err.println("Sine Curve Simulator " + System.currentTimeMillis());
		HashMap<String, Integer> latencyMap = new HashMap<String, Integer>();
		final EnumMap<PowerTransitionGenerator.PowerStateKind, Map<String, PowerState>> transitions = PowerTransitionGenerator
				.generateTransitions(20, 280, 490, 25, 35);

		// The definition of the PM
		PhysicalMachine pm = new PhysicalMachine(machinecores, 5, 256000000000l,
				new Repository(5000000000000l, "PMID", 250000000, 250000000, 50000000, latencyMap,
						transitions.get(PowerTransitionGenerator.PowerStateKind.storage),
						transitions.get(PowerTransitionGenerator.PowerStateKind.network)),
				89000, 29000, transitions.get(PowerTransitionGenerator.PowerStateKind.host));
		// The virtual machine image to be used for the future VM
		VirtualAppliance va = new VirtualAppliance("test", 30000, 0, false, 100000000);

		// Placing the VM's image on the PMs local disk
		pm.localDisk.registerObject(va);
		pm.turnon();

		Timed.simulateUntilLastEvent();

		// by this time the PM is on
		final VirtualMachine vm = pm.requestVM(va, pm.getCapacities(), pm.localDisk, 1)[0];
		Timed.simulateUntilLastEvent();
		// by this time the VM is running on the PM

		basetime = Timed.getFireCount();
		final MonitorConsumption consumptionMonitor = new MonitorConsumption(vm, 5);
		System.err.println("PM and VM is prepared " + System.currentTimeMillis());
		// Parsing the trace characteristics for Job

		jobs = tryJobs();
		Collections.sort(jobs, JobListAnalyser.submitTimeComparator);
		jobslen = jobs.size();

		// For every job we record its completion in the jobhits. This event
		// adapter is created so it can catch the job completion events.
		final ConsumptionEventAdapter cae = new ConsumptionEventAdapter() {
			@Override
			public void conComplete() {
				jobhits++;
			}
		};
		// JSender starts up the jobs on our single VM once the trace demands it
		class JSender extends Timed {
			int currentcount = 0;

			public JSender() {
				// Makes sure the first event will come upon the first job's
				// arrival.
				subscribe(getConvertedFirecount(currentcount) - Timed.getFireCount());
			}

			// Transforms job arrival times to be in the VMs lifetime
			public long getConvertedFirecount(int count) {
				return count >= jobslen ? -1 : jobs.get(count).getSubmittimeSecs() * 1000 + basetime;
			}

			@Override
			public void tick(long fires) {
				long nextSubscriptionTime;
				int count = 0;
				do {
					try {
						Job j = jobs.get(currentcount);

						// Injects a new job when its due

						vm.newComputeTask(j.getExectimeSecs(), j.nprocs, cae);

						//System.out.println("Base Time: " + basetime);
						
						System.out.println("Count: " + currentcount);

						System.out.println("SubHour Consumption: " + consumptionMonitor.getSubHourProcessing());

						System.out.println("Subsecond Consumption: " + consumptionMonitor.getSubSecondProcessing());

					} catch (Exception e) {
						throw new IllegalStateException(e);
					}
					currentcount++;

				} while ((nextSubscriptionTime = getConvertedFirecount(currentcount)) == fires);
				
				if (nextSubscriptionTime > fires) {
					// Calculates when the next job is going to be due and
					// orders an event for that occasion
					updateFrequency(nextSubscriptionTime - fires);
				} else {
					// Ensures that the events are not coming after there are no
					// further jobs
					unsubscribe();
				}
			}

		}
		// Starts up the job submission process
		new JSender();
		long before = System.currentTimeMillis();
		System.err.println("Jobs are prepared " + before);
		// The simulation is now ready to be executed
		Timed.simulateUntilLastEvent();
		// Statistics printouts
		long after = System.currentTimeMillis();
		System.err.println("Final job has completed, took " + (after - before) + " ms");
		long afterSimu = Timed.getFireCount();
		System.err.println("Total simulated time " + (afterSimu - basetime) + " ms");
		if (jobhits == jobs.size()) {
			System.err.println("All jobs terminated successfully..");
		} else {
			System.err.println("Not completed all jobs! Successful completions: " + jobhits);
		}

	}

	public static void main(String args[]) {
		try {

			loadTraceUntoVM();
		} catch (Exception e)

		{
			e.getMessage();
		}

	}

}