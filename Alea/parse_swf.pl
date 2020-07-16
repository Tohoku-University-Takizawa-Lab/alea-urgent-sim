#!/usr/bin/perl
#
# example Perl script to extract job info from a log in SWF.
# then you can tabulate various statistics about something of interest.
#
# the data format is one line per job, with 18 fields:
#  0 - Job Number
#  1 - Submit Time
#  2 - Wait Time
#  3 - Run Time
#  4 - Number of Processors
#  5 - Average CPU Time Used
#  6 - Used Memory
#  7 - Requested Number of Processors
#  8 - Requested Time
#  9 - Requested Memory
# 10 - status (1=completed, 0=killed)
# 11 - User ID
# 12 - Group ID
# 13 - Executable (Application) Number
# 14 - Queue Number
# 15 - Partition Number
# 16 - Preceding Job Number
# 17 - Think Time from Preceding Job
#

use warnings;
use strict;
use Time::Local;
use POSIX;
my $oldhandle = select(STDERR);
$| = 1;  # Turn off buffering on STDOUT
select($oldhandle);

# count bad things
my $cnt_fmt  = 0;
my $cnt_t0   = 0;
my $cnt_p0   = 0;
my $cnt_stat = 0;
my $cnt_bad  = 0;

# some useful globals
my $start;
my $jobs;
my $procs;
my $nodes;

#
# scan trace and collect job info
#
while (<>) {

    #
    # empty or comment line
    #
    # such lines are skipped, but note that some header comments
    # may include useful data
    #
    if (/^\s*$|^;/) {

	# maintain data about log start time
	if (/^;\s*UnixStartTime:\s*(\d+)\s*$/) {
	    $start = $1;
	}
	if (/^;\s*TimeZoneString:\s*([\w\/]+)\s*$/) {
	    $ENV{TZ} = $1;
	    POSIX::tzset();
	}
	# about jobs
	if (/^;\s*MaxJobs:\s*(\d+)\s*$/) {
	    $jobs = $1;
	    printf(STDERR "there are $jobs jobs\n");
	}
	# and about system size
	if (/^;\s*MaxProcs:\s*(\d+)\s*$/) {
	    $procs = $1;
	}
	if (/^;\s*MaxNodes:\s*(\d+)\s*$/) {
	    $nodes = $1;
	}

        next;
    }

    #
    # parse job line
    #
    $_ =~ /^\s*(.*)\s*$/;
    my $line = $1;

    my @fields = split(/\s+/,$line);
    if ($#fields != 17) {
	warn "bad format at $line";
	$cnt_fmt++;
    }

    # or alternatively
    my ($job, $sub, $wait, $t, $p, $cpu, $mem, $preq, $treq, $mreq,
	$status, $u, $gr, $app, $q, $part, $prec, $think) = split(/\s+/,$line);

    # show progress...
    if ($job % 1000 == 0) {
	printf(STDERR "\rdid job $job...");
    }

    #
    # skip if this job is not meaningful
    #
    if ( ! ($job =~ /^\s*\d/)) {
	# not a job at all -- line does not start with job ID.
	$cnt_fmt++;
	next;
    }

    if ($t == 0) {
	# someting potentially fishy, as job took 0 time.
	# but this can also be a resolution problem.
	$cnt_t0++;
	#next;
    }

    if ($p == 0) {
	# someting really fishy: job did not use any processors.
	# could mean job was cancelled before running.
	$cnt_p0++;
	next;
    }

    if (($sub == -1) || ($t == -1) || ($p == -1)) {
	# something very fishy: job arrival, runtime, or processors undefined.
	$cnt_bad++;
	next;
    }

    if ($status != 1) {
	# another fishy:
	# job failed (status 0)
	# job was cancelled (status 5)
	# or job is only part of a whole job (status 2, 3, 4)
	$cnt_stat++;
	next;
    }

    # example of parsing submit time
    $sub += $start;
    my ($sec,$min,$hr,$mday,$mon,$year,$wday,$yday,$isdst) = localtime($sub);
    $year += 1900;
    $mon += 1;	# $mon is 0-based for use as index to array; add 1 to get month number
    $wday += 1;	# also 0-based, add 1 to get day number

    # example of printing submit, processors, and runtime about first 10 jobs
    if ($job <= 10) {
	printf("%s %3d %5d\n",
	       strftime("%d/%m/%y-%H:%M:%S", localtime($sub)),
	       $p, $t
	      );
    }

    #
    # COLLECT DATA
    #

    # your code here...

}

printf(STDERR "\n");
printf(STDERR "$cnt_fmt lines had a bad format\n");
printf(STDERR "$cnt_t0 jobs had 0 time\n");
printf(STDERR "$cnt_p0 jobs had 0 processors\n");
printf(STDERR "$cnt_stat jobs had non-1 status\n");
printf(STDERR "$cnt_bad jobs had bad data (undefined arrival, runtime, or processors)\n");