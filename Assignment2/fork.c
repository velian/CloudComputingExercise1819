#include<stdio.h>
#include<stdlib.h> 
#include<unistd.h> 

void calcsubcomponent(int low_value, int high_value);

int main(int argc, char *argv[]){

	if(atoi(argv[1]) >= atoi(argv[2])){
		exit(-1);
	}

	int startingvalue = atoi(argv[1]);
	int endvalue = atoi(argv[2]);
	calcsubcomponent(startingvalue, endvalue);
}

void calcsubcomponent(int low_value, int high_value){

	if (! (low_value == high_value)){
		int pip1[2];
		int pip2[2];
		int pid;
		int wpid;
		int status = 0;
		char buf[50];
		int result;

		if (pipe(pip1) < 0){
			exit(1);
		}
		if (pipe(pip2) < 0){
			exit(1);
		}

		pid = fork();
		if ( !(pid == 0) ){
			pid = fork();
			if (pid == 0){
				low_value = ((low_value + high_value)/2) + 1;
				dup2(pip2[1], 1);
				//close(pip[0]);
				//printf("created 2 fork using values %d and %d \n", low_value, high_value);
				calcsubcomponent(low_value, high_value);
			}
		}else{
			high_value = ((low_value + high_value)/2);
			dup2(pip1[1], 1);
			//close(pip[0]);
			//printf("created 1 fork using values %d and %d \n", low_value, high_value);
			calcsubcomponent(low_value, high_value);
		}

		//printf("entering wait \n");
		while ((wpid = wait(&status)) > 0 );
		//printf("wait termination \n");
		read(pip1[0], buf, sizeof buf);
		result = atoi(buf);
		//printf("read from buffer %d \n", result);
		read(pip2[0], buf, sizeof buf);
		result = result + atoi(buf);
		//printf("addition from buffer %d \n");
		printf("%d\n", result);
		exit(0);

	}else{
		printf("%d\n", low_value);
		exit(0);
	}

	return;
	
}