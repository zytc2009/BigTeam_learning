### 题目：

给定一个有n个正整数的数组A和一个整数sum,求选择数组A中部分数字和为sum的方案数。

当两种选取方案有一个数字的下标不一样，就认为是不同的组成方案。

分析：

首先确定选择的个数，有n种这样的选择，分别是选择1个数，2个数...n个数。分别对应m1个选择方案，m2个选择方案...mn个选择方案。

所以，我们先写这样一个方法，计算m1+m2+...+mn，和就是我们求的总的方案数。

getAllSchemeNum需要两个参数，也就是两个初始条件，一个是数组A,一个是和sum.



现在，重点是如何编写getSchemeNumIfChooseM方法。

如果我们选择k个数，并规定这些数的顺序与数组中的顺序相同，那么只要确定每个数的位置就ok了，我们先确定第一个数的下标，范围是[0,n-k]，为什么可选下标的右边界是n-k，因为如果第一个数的下标是n-k+1，那么后面只有k-2个位置，放不下剩余的k-1个值。

假定第一个数选择的下标为b1(b1<=n-k)，再选第2个数的位置，第二个数只能在第一个数之后，因此它可选的下标范围[b1+1,n-k+1];

依次类推，直到第k个数被选定。

用递归是较好的办法。

每次递归，必须给出，数组A，还要选定的数的个数，可选下标的左边界，以及还要选定的数之和。



### 完整及测试代码如下：


	public class Test {
		public static void main(String[] args) throws Exception 
		{
			int[] arr ={5,10,5,2,3,4,6};
			System.out.println("方案数:"+getAllSchemeNum(arr,15));
		}
		
		public static int getAllSchemeNum(int[] arr,int sum)
		{
			int count=0;
			//将选择一个数，两个数...n个数时的方案数相加
			for(int num=1;num<=arr.length;num++)
			{
				//getNumIfChosseM是选择m个数时得到的方案数
				count+=getSchemeNumIfChooseM(arr,num,0,sum);
			}
			return count;
		}
		
		/**
		 * 
		 * @param arr		数组A
		 * @param num		还需要选择的数的个数
		 * @param index		可选的范围的左边界
		 * @param sum		还需要选择数之和
		 * @return
		 */
		public static int getSchemeNumIfChooseM(int[] arr, int num,int index,int sum)
		{
			int count=0;
			
			//如果全部选择完成，则只需判定sum是否为零，如果为零，符合条件，返回1，否则返回0
			if(num==0)
			{
				return sum==0?1:0;
			}
			//剩余要选的数里，第一个数可选的范围为[index,arr.length-num]
			for(int i=index;i<=arr.length-num;i++)
			{
				if(arr[i]<=sum)
					//可选的个数减一，可选的左边界等于当前确定数的小标加1,
					count+=getSchemeNumIfChooseM(arr,num-1,i+1,sum-arr[i]);
			}
			
			return count;
		}
	}






原文链接：https://blog.csdn.net/HIT_lk/java/article/details/53967627