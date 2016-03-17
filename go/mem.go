package main

import (
	"fmt"
	"os"
	"strconv"
	"sync"
	"time"

	"github.com/bradfitz/gomemcache/memcache"
)

func add(wg *sync.WaitGroup, mc *memcache.Client, c int, b []byte) {
	item := new(memcache.Item)
	item.Expiration = 3600
	item.Value = b

	for i := 0; i < c; i++ {
		item.Key = "key" + strconv.Itoa(i)
		_ = mc.Add(item)

	}
	wg.Done()
}

func get(wg *sync.WaitGroup, mc *memcache.Client, c int) {
	for i := 0; i < c; i++ {
		mc.Get("key" + strconv.Itoa(i))
	}
	wg.Done()
}

func gets(wg *sync.WaitGroup, mc *memcache.Client, c int) {
	for i := 0; i < c; i = i + 100 {
		keys := make([]string, 100)
		for j := 0; j < 100; j++ {
			keys[j] = "key" + strconv.Itoa(i+j)
		}
		mc.GetMulti(keys)
	}
	wg.Done()
}

func set(wg *sync.WaitGroup, mc *memcache.Client, c int, b []byte) {
	item := new(memcache.Item)
	item.Expiration = 3600
	item.Value = b

	for i := 0; i < c; i++ {
		item.Key = "key" + strconv.Itoa(i)
		mc.Set(item)
	}
	wg.Done()
}

func main() {
	mc := memcache.New("127.0.0.1:11211")
	mc.Timeout = 10 * time.Second

	bytes := make([]byte, 1024)
	var wg sync.WaitGroup
	parallelLevel := 100
	c, _ := strconv.Atoi(os.Args[1])
	fmt.Printf("parallelLevel: %d, count: %d\n", parallelLevel, c)

	memTest(mc, bytes, parallelLevel, c, 32, &wg)
	memTest(mc, bytes, parallelLevel, c, 512, &wg)
	memTest(mc, bytes, parallelLevel, c, 1024, &wg)
}

func memTest(mc *memcache.Client, bytes []byte, p int, c int, dataLength int, wg *sync.WaitGroup) {
	fmt.Printf("=============Test with %d bytes ============\n", dataLength)

	t := time.Now()
	for i := 0; i < p; i++ {
		wg.Add(1)
		go add(wg, mc, c, bytes[0:dataLength])
	}
	wg.Wait()
	fmt.Printf("add: %d ms\n", int(time.Now().Sub(t).Nanoseconds()/1000000))

	t = time.Now()
	for i := 0; i < p; i++ {
		wg.Add(1)
		go get(wg, mc, c)
	}
	wg.Wait()
	fmt.Printf("get: %d ms\n", int(time.Now().Sub(t).Nanoseconds()/1000000))

	t = time.Now()
	for i := 0; i < p; i++ {
		wg.Add(1)
		go gets(wg, mc, c)
	}
	wg.Wait()
	fmt.Printf("gets: %d ms\n", int(time.Now().Sub(t).Nanoseconds()/1000000))

	t = time.Now()
	for i := 0; i < p; i++ {
		wg.Add(1)
		go set(wg, mc, c, bytes[0:dataLength])
	}
	wg.Wait()
	fmt.Printf("set: %d ms\n", int(time.Now().Sub(t).Nanoseconds()/1000000))

	fmt.Println()
}
