## Urgent-Sim: Preemptive Parallel Job Scheduling for Heterogeneous Systems Supporting Urgent Computing

This is the code repository for the job scheduler proposed in the following paper:

Agung, M., Watanabe, Y., Weber, H., Egawa, R. and Takizawa, H., 2021. Preemptive parallel job scheduling for heterogeneous systems supporting urgent computing. IEEE Access, 9, pp.17557-17571. https://doi.org/10.1109/ACCESS.2021.3053162

## Usage

Prerequisite:
- JDK8 and later

Usage:
    
    $ ant compile
    $ ant run -Dapplication.args=<conf_file>

Urgent-Sim is built on top of Alea-GridSim simulation platform:
https://github.com/aleasimulator/alea

## How to cite
If you find this work useful in your research, please cite the paper using this bibtex reference:

```
    @ARTICLE{9328750,  author={Agung, Mulya and Watanabe, Yuta and Weber, Henning and Egawa, Ryusuke and Takizawa, Hiroyuki},
    journal={IEEE Access},
    title={Preemptive Parallel Job Scheduling for Heterogeneous Systems Supporting Urgent Computing},
    year={2021},
    volume={9},
    number={},
    pages={17557-17571},
    doi={10.1109/ACCESS.2021.3053162}}
