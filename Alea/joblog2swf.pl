#!/usr/bin/perl
#
# scan job execution log files from the SDSC joblog repository,
# and use convert2swf.pm to convert the data into the standard workload format.
# this version specifically tailored for the SDSC SP2.
#

use warnings;
use strict;
use Time::Local;
use POSIX;
use convert2swf_v4;


############################
#
# DO THE CONVERSION:
#

my $tz_str    = "US/Pacific";
my $max_procs = 128;
my @queues    = ("interactive",
		 "express",
		 "high",
		 "normal",
		 "low",
		 "standby",
		);


SWF::init( $tz_str, $max_procs, \@queues );

my $format_bugs;
my $jobs_data = get_jobs();

my ($zero, $maxtime) = SWF::convert( $jobs_data, $format_bugs );

my $header = generate_header( $zero, $maxtime, $max_procs );

SWF::print( $header );



#####################################
# SPECIFICS for SDSC joblog workloads

# the format of each entry includes the following comma-separated fields:
#  0  user (encrypted)
#  1  account (encrypted)
#  2  submit time (seconds since epoch)
#  3  start time (seconds since epoch)
#  4  end time (seconds since epoch)
#  5  queue
#  6  CPU time (seconds)
#  7  wallclock processor secconds
#  8  seconds charged
#  9  sum of nodes
# 10  max nodes (same as above)
# 11  number of steps (always 1)
# 12  memory high water mark
# 13  memory usage KB-hours
# 14  I/O in MB transferred
# 15  disk charge (?)
# 16  interactive connect time
# 17  wait time in queue (for batch)
# 18  slowdown (wait+wallclock/wallclock)
# 19  priority
# 20  application
# 21  job ID on originating system
# 22  job ID from queueing system
# 23  submit (for humans)
# 24  end (for humans)
# 25  requested wallclock time
# 26  requested memory
# 27  requested nodes
# 28  status (1=success, 2=cancelled, 3=unknown)

my $job_cnt;


sub get_jobs {
##############
# parse input format and create a hash for each job
#
# note: the original format provides times in human-readable format (hh:mm:ss),
# but these seem to be wrong, and specifically the result of using gmtime instead
# of localtime. so here we also need to use gmtime.

    my @list_of_jobs;

    #
    # scan trace and collect job info
    #
    while (<>) {

	#
	# parse line and extract info
	#

	if (/^\s*$/) {		# empty line
	    next;
	}

	$_ =~ /\s*(.*)\s*/;		# get rid of white space at ends
	$_ = $1;
	my @line = split(',');		# split into fields
	if ($#line != 28) {
	    warn(">>>Format problem on $_\ngot $#line fields (should be 29)");
	    next;
	}

	my %nj;

	#
	# set the desired fields as specified in convert2swf.pm
	#

	$nj{trace}  = $_;	# used for error messages


	# map status first
	my $status = 0;
	if ($line[28] == 1) {
	    $status = 1;
	}
	elsif ($line[28] == 2) {
	    $status = 5;
	}
	elsif ($line[28] != 3) {
	    # 3 means unknown...
	    $format_bugs .= "unknown_status_problem [$line[28]] $_\n";
	}
	if ($status != 0) {
	    $nj{status} = $status;
	}


	# get submit, start, and end times
	my $submit = $line[2];
	$nj{submit} = $submit;

	my $start = $line[3];
	if ($start == 0) {
	    if ($status == 1) {
		# job is OK -- a real problem
		$format_bugs .= "start_zero $_\n";
	    }
	    # removed before started
	    $start = -1;
	}
	$nj{start} = $start;

	# check:
	my $wait = $line[17];
	if (($submit > 0) && ($start > 0) && ($wait != -1)) {
	    my $calc_wait = $start - $submit;
	    if ($calc_wait > $wait) {
		$format_bugs .= "wait_short_problem [s-s=$calc_wait w=$wait] $_\n";
	    }
	    if ($calc_wait < $wait) {
		$format_bugs .= "wait_long_problem [s-s=$calc_wait w=$wait] $_\n";
	    }
	}

	my $end = $line[4];
	$nj{end} = $end;

	# number of processors
	my $procs = $line[10];
	$nj{procs} = $procs;

	# CPU time: convert to average
	my $cpu = -1;
	if ($procs > 0) {
	    $cpu = $line[6] / $procs;
	}
	$nj{cpu} = $cpu;

	# memory usage
	my $mem = $line[12];
	$nj{mem} = $mem;

	# requested number of processors
	my $req_procs = $line[27];
	$nj{req_procs} = $req_procs;

	# requested runtime
	my $req_time = $line[25];
	$nj{req_time} = $req_time;

	# requested memory
	my $req_mem = $line[26];
	$nj{req_mem} = $req_mem;

	# user
	my $user = $line[0];
	$nj{user} = $user;

	# group
	my $group = $line[1];
	$nj{group} = $group;

	# application
	my $app = $line[20];
	$nj{app} = $app;

	# queue
	my $queue = $line[5];
	$nj{queue} = $queue;

	$list_of_jobs[$job_cnt++] = \%nj;
    }

    return \@list_of_jobs;
}


sub generate_header {
#####################
    my ($zero, $maxtime, $max_procs) = @_;

    my ($sec, $min, $hr, $mday, $mon, $year, $wday, $yday, $isdst);

    $header = "; Version: 2.2
; Computer: IBM SP2
; Installation: SDSC
; Acknowledge: Victor Hazlewood
; Copyright: JOBLOG Repository Data Usage Agreement:
;   This Job Trace Repository is brought to you by the HPC Systems
;   group of the San Diego Supercomputer Center (SDSC), which is the
;   leading-edge site of the National Partnership for Advanced
;   Computational Infrastructure (NPACI).
;   The JOBLOG data is Copyright 2000 The Regents of the University of
;   California All Rights Reserved.
;   Permission to use, copy, modify and distribute any part of the
;   JOBLOG data for educational, research and non-profit purposes,
;   without fee, and without a written agreement is hereby granted,
;   provided that this copyright notice is preserved in all copies and
;   all works based on use or analysis of this data is properly
;   referenced in any written or electronic publication.
; Information: http://joblog.npaci.edu/
;              http://www.cs.huji.ac.il/labs/parallel/workload/
";

    $header .= sprintf("; Conversion: Dror Feitelson (feit\@cs.huji.ac.il) %s\n",
		       strftime("%d %b %Y", localtime()));

    $header .= sprintf("; MaxJobs: %d\n", $job_cnt);
    $header .= sprintf("; MaxRecords: %d\n", $job_cnt);
    $header .= "; Preemption: No\n";

    $header .= sprintf("; UnixStartTime: %d\n", $zero);
    $header .= "; TimeZone: -28800\n";
    $header .= sprintf("; TimeZoneString: %s\n", $tz_str);

    $header .= sprintf("; StartTime: %s\n",
	   strftime("%a %b %2d %H:%M:%S %Z %Y", localtime($zero)));

    $header .= sprintf("; EndTime:   %s\n",
	   strftime("%a %b %2d %H:%M:%S %Z %Y", localtime($maxtime)));

    $header .= "; MaxNodes: $max_procs\n";
    $header .= "; MaxProcs: $max_procs\n";

    return $header;
}