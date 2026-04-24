#include "./utils.hpp"

#include <iostream>

using namespace std;

void print(vector<char> v){
    for(char c : v){
        cout << c;
    }
    cout << "\n";

}

void print(string s){
    for(char c : s){
        cout << c;
    }
    cout << "\n";
}

void print(char c){
    cout << c;
    cout << '\n';
}

void print(int i){
    cout << i;
    cout << '\n';
}

void print(float f){
    cout << f;
    cout << '\n';
}
