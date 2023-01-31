

#### 1.setjmp()函数

创建本地的jmp_buf缓冲区并且初始化，用于将来跳转回此处。这个宏可能不只返回一次。第一次，在直接调用它时，它总是返回零。当调用 longjmp 时带有设置的环境信息，这个宏会再次返回，此时它返回的值会传给 longjmp 作为第二个参数。

```c
#include <stdio.h>
#include <setjmp.h>
jmp_buf BUFFER; 
void handle_error(){
	int err_code = setjmp(BUFFER);
	if(err_code != 0){
		printf("Error code: %d\n", err_code);
	}
}
 
void trigger_error(int err_code){
	longjmp(BUFFER, err_code);
}
 
int main()
{
	handle_error();
	trigger_error(1);
	trigger_error(2); 
	return 0;
}
输出：
    Error code: 1
    Error code: 2
```

