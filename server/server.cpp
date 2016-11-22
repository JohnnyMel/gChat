#include <iostream>
#include <vector>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <sys/file.h>
#include <pthread.h>
#include <netinet/in.h> 
#include <sstream>
#include <semaphore.h>
#include <unistd.h>

unsigned int total_con = 0;

pthread_mutex_t mut = PTHREAD_MUTEX_INITIALIZER;
sem_t sem;

using namespace std;

vector<int> sid;

void *echo_function (void *arg)
{
  int so = (int) arg;
  int n; 
 
   printf("Connected.\n");
   pthread_mutex_lock(&mut);
   total_con++;
   pthread_mutex_unlock(&mut);
   sem_post(&sem);         
    do 
    {
        char *str = new char[512];
        
	if ((n = recv(so,str, 512, 0)) < 1) 
	  break;
	
	for(unsigned int i=0; i<sid.size(); i++)
	  if(sid.at(i) != so)
	     send(sid.at(i), str, n, 0);
	str[n] = '\0';
	printf("%s",str);
	delete[] str;
    }
    while (true);

    close(so);
    for(unsigned int i=0; i<sid.size(); i++)
    {
	if(sid.at(i) == so)
	  sid.erase(sid.begin() + i);
    }
    sem_post(&sem);
    pthread_exit(0);
}

void *count ()
{
  
  while(true)
  {
    sem_wait(&sem);
    if(total_con > sid.size())
      total_con = sid.size();
    ostringstream conv;
    conv << total_con;
    string str = conv.str();
    str = str + "\n";
    char *st = str.c_str();
    cout << st << endl;
    for(unsigned int i=0; i<sid.size(); i++)
      send(sid.at(i), st, str.size(), 0);
 }
  pthread_exit(0);
}


int main()
{
  int s, s2, portno = 5199;
  struct sockaddr_in local, remote;
  pthread_t thread[1500];
  sem_init(&sem, 0, 0);
  
  if ((s = socket(AF_INET, SOCK_STREAM, 0)) == -1) 
  {
    perror("socket");
    exit(1);
   }
        
   bzero((char *) &local, sizeof(local));

   local.sin_family = AF_INET;
   local.sin_port = htons(portno);
        
   local.sin_addr.s_addr = INADDR_ANY;
	
    if (bind(s, (struct sockaddr *)&local, sizeof(local)) == -1) 
    {
      perror("bind");
      exit(1);
     }

     if (listen(s, 5) == -1) 
     {
       perror("listen");
       exit(1);
      }
      pthread_create(&(thread[0]), NULL, count, NULL);
      
      for(int i = 1; ;i++) 
      {
	printf("Waiting for a connection...\n");
        int t = sizeof(remote);
        if ((s2 = accept(s, (struct sockaddr *)&remote, &t)) == -1) 
	{
           perror("accept");
           exit(1);
        }
        pthread_create(&(thread[i]), NULL, echo_function, (void *)s2);
	
	sid.push_back(s2);
	for(unsigned int j=0; j<sid.size(); j++)
	  cout << sid.at(j) << " ";
      }

       return 0;
}
