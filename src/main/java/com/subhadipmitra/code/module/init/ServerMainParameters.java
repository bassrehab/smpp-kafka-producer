package com.subhadipmitra.code.module.init;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.ArrayList;
import java.util.List;

/**
 * SMPP Server command line options. Annotation driven.
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 07/09/17.
 **/
@Parameters(commandDescription = "SMPP Server command line options")
public class ServerMainParameters {
	
	private List<String> smscPorts;

	public List<String> getSmscPorts() {
		return smscPorts;
	}

	@Parameter(names={"-p", "-port"}, description="List of SMSC ports", required=true, variableArity=true)
	public void setSmscPorts(List<String> smscPorts) {
		this.smscPorts = smscPorts;		
	}
	
	public List<Integer> getSmscPortsAsIntegers() {
		List<Integer> smscPortsInts = new ArrayList<Integer>(smscPorts.size());
		for (String str : smscPorts) {
			smscPortsInts.add(new Integer(str));
		}
		return smscPortsInts;
	}

	private boolean help; 
	
	@Parameter(names = {"--help", "?", "help"}, description="Shows usage", help = true)
	public void setHelp(boolean help) {
		this.help = help;
	}
	
	public boolean isHelp() {
		return help;
	}

}
