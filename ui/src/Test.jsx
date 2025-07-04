import React, { useEffect } from 'react'

function Test() {
  useEffect(() => {
    const tst = async () => {
      try {
        const res = await fetch("http://localhost:8083/api/v1/messages/599ad5a7-620d-4866-96b5-b81063bc7537")
        const data = await res.json()
        console.log(data) // Log ra kết quả từ API
      } catch (error) {
        console.error("Error fetching data:", error)
      }
    }

    tst() // Gọi hàm để nó chạy
  }, [])

  return <div>Test Component</div>
}

export default Test
