 
 import com.facebook.infer.annotation.*; 
            
                      @ThreadSafe                      
                      class Test { 
                          A myA  = new A(); 
                          A myA1 = new A(); 
                          A a    = new A(); 
                          A x    = new A(); 
                          A b    = new A(); 
  
                          void t1(int y){ 
                              synchronized(x){ 
                                  synchronized (a){ 
                                      myA.f = y; 
                                  } 
                              } 
                          } 
  
  
                          void t2(int y){ 
                              synchronized(x){ 
                                  synchronized (b){ 
                                      myA.f = y; 
                                  } 
                              } 
                          } 
  
                          void t3(int y){ 
                                  synchronized(b) { 
                                      myA.f = y; 
                                  } 
                          } 
  
                          void t4(A a){ 
                              synchronized(x){ 
                                      synchronized(b) { myA = a; }  
                               
                              } 
                          } 
                      } 
  
                      class A { int f = 0;  }