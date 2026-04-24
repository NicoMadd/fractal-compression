#include "executors.hpp"

#include <utility>

using namespace std;


Executor::Executor(int parallelism) : parallelism(parallelism) {
    for (int i = 0; i < parallelism; i++) {
        this->threads.push_back(thread([this]() {
            while (true) {
                function<void()> task;
                {
                    unique_lock<mutex> lock(this->tasks_mutex);
                    this->task_available.wait(lock, [this]() {
                        return this->shutdown_requested || !this->tasks.empty();
                    });
                    if (this->shutdown_requested && this->tasks.empty()) {
                        break;
                    }
                    task = std::move(this->tasks.front());
                    this->tasks.pop();
                }
                task();
            }
        }));
    }
}


void Executor::submit(function<void()> task) {
    {
        unique_lock<mutex> lock(this->tasks_mutex);
        if (this->shutdown_requested) {
            return;
        }
        this->tasks.push(std::move(task));
    }
    this->task_available.notify_one();
}

void Executor::shutdown() {
    {
        unique_lock<mutex> lock(this->tasks_mutex);
        this->shutdown_requested = true;
    }
    this->task_available.notify_all();
}

void Executor::join() {
    for (int i = 0; i < this->parallelism; i++) {
        if (this->threads[i].joinable()) {
            this->threads[i].join();
        }
    }
}

Executor::~Executor() {
    this->shutdown();
    this->join();
}
