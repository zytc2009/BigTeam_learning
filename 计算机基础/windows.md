### 1.窗口属性

```cpp
//避免被录制
SetWindowDisplayAffinity(win_id, WDA_EXCLUDEFROMCAPTURE);


// 获取窗口的最小化位置和大小
WINDOWPLACEMENT placement;
placement.length = sizeof(WINDOWPLACEMENT);
GetWindowPlacement(hwnd, &placement);

// 检查窗口是否最小化
if (placement.showCmd == SW_SHOWMINIMIZED) {
        // 获取最小化窗口的实际位置和大小
        RECT minimizedRect = placement.rcNormalPosition;
        int width = minimizedRect.right - minimizedRect.left;
        int height = minimizedRect.bottom - minimizedRect.top;
        std::cout << "窗口最小化位置和大小：" << std::endl;
        std::cout << "左上角坐标：(" << minimizedRect.left << ", " << minimizedRect.top << ")" << std::endl;
}
```

### 2.窗口控制

```c++
HWND hwnd = (HWND)0x009B056C;
//取当前窗口位置
RECT rect;
GetWindowRect(hwnd, &rect);
// 计算新的窗口位置
int newX = 1296;  // 新的窗口左上角 x 坐标
int newY = 500;   // 新的窗口左上角 y 坐标

//恢复窗口
ShowWindow(hwnd, SW_RESTORE);
//移动窗口
SetWindowPos(hwnd, NULL, newX, newY, 0, 0, SWP_NOSIZE | SWP_NOZORDER);
```

### 3.鼠标点击模拟：

```c++
void MouseClick(int x, int y) {
    // 获取屏幕尺寸
    int screenWidth = GetSystemMetrics(SM_CXSCREEN);
    int screenHeight = GetSystemMetrics(SM_CYSCREEN);

    // 创建鼠标输入事件
    INPUT input;
    input.type = INPUT_MOUSE;
    input.mi.dx = x * (65536 / screenWidth);
    input.mi.dy = y * (65536 / screenHeight);
    input.mi.dwFlags = MOUSEEVENTF_MOVE | MOUSEEVENTF_ABSOLUTE | MOUSEEVENTF_LEFTDOWN | MOUSEEVENTF_LEFTUP;

    // 发送鼠标输入事件
    SendInput(1, &input, sizeof(INPUT));
}
```

### 4. 窗口最小化后保持录制

1. 首先，您需要使用 GDI（图形设备接口）函数来捕获窗口的内容。您可以使用 `GetWindowDC` 函数获取窗口的设备上下文（Device Context）句柄。
2. 然后，使用 GDI 函数 `BitBlt` 将窗口的内容复制到一个位图（Bitmap）对象中。您可以创建一个兼容的位图对象，使用 `CreateCompatibleBitmap` 函数创建，并使用 `SelectObject` 函数将位图对象选入设备上下文。
3. 在窗口最小化后，您可以继续使用 GDI 函数 `BitBlt` 将窗口内容复制到位图对象中。这样，您就可以继续录制窗口的内容，即使窗口不可见。
4. 最后，您可以保存位图对象中的图像数据，或者进行其他操作，例如实时显示、保存为视频等。

```
#include <iostream>
#include <windows.h>
int main() {
    // 获取窗口句柄
    HWND hwnd = GetForegroundWindow();

    // 获取窗口的设备上下文
    HDC windowDC = GetWindowDC(hwnd);

    // 创建兼容的位图对象
    HDC memoryDC = CreateCompatibleDC(windowDC);
    HBITMAP bitmap = CreateCompatibleBitmap(windowDC, width, height);
    HBITMAP oldBitmap = SelectObject(memoryDC, bitmap);

    // 录制窗口的内容
    BitBlt(memoryDC, 0, 0, width, height, windowDC, 0, 0, SRCCOPY);

    // 进行其他操作，例如保存位图数据或实时显示

    // 清理资源
    SelectObject(memoryDC, oldBitmap);
    DeleteObject(bitmap);
    DeleteDC(memoryDC);
    ReleaseDC(hwnd, windowDC);

    return 0;
}
```

