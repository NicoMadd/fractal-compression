#pragma once

#include <condition_variable>
#include <functional>
#include <mutex>
#include <queue>
#include <thread>
#include <vector>

using namespace std;


class Executor {
  public:
    Executor(int parallelism);
    ~Executor();
    void submit(function<void()> task);
    void shutdown();
    void join();

  private:
    int parallelism;
    vector<thread> threads;
    queue<function<void()>> tasks;
    mutex tasks_mutex;
    condition_variable task_available;
    bool shutdown_requested = false;
};
