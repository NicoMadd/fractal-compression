#include "metadata.hpp"

#include <iostream>
#include <fstream>

ImageMetadata loadImage(string imagePath){

    ifstream file(imagePath);

    char* foo = new char[40];
    
    file.read(foo, 40);

    cout << foo;
    delete[] foo;

    return ImageMetadata();
}
