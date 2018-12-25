#include <iostream>
#include <string>

using namespace std;

void tf() {
    if (0) {
        cout << "no" << endl;
    }
}

int main(int argc, char** argv) {
    
    int a;
    cin >> a;
    if (a == 1) {
        cout << "input 1" << endl;
    } else {
        cout << "else" << endl;
    }

    tf();

    return 0;
}
