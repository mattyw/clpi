(ns clpi.core)
(import '[java.net DatagramSocket
          DatagramPacket
          MulticastSocket
          InetAddress
          InetSocketAddress])

(defn send [^DatagramSocket socket msg host port]
  (let [payload (.getBytes msg)
        length (min (alength payload) 512)
        address (InetSocketAddress. host port)
        packet (DatagramPacket. payload length address)]
    (.send socket packet)))

(defn receive [^DatagramSocket socket]
  (let [buffer (byte-array 512)
        packet (DatagramPacket. buffer 512)]
    (.receive socket packet)
    {:data (String. (.getData packet)
             0 (.getLength packet))
     :address (-> packet .getAddress .toString)}))

(defn receive-loop [socket f]
  (future (while true (f (receive socket)))))

(defn handle-message [socket msg]
  (println msg)
  (send socket "ping" "224.0.0.1" 9999)
  (Thread/sleep 10000))

(defn -main []
  (let [group (InetAddress/getByName "224.0.0.1")
        socket (MulticastSocket. 9999)]
    (.joinGroup socket group)
    (receive-loop socket (fn [msg] (handle-message socket msg)))))
