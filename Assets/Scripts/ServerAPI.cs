using Newtonsoft.Json;
using System;
using System.Collections;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using UnityEngine;

public class ServerAPI : MonoBehaviour
{
    public static Socket socket = null;
    public static string Proc_Id = null;

    private static int LISTEN_INTERVAL = 1;

    // Unity's main thread
    Thread mainThread = Thread.CurrentThread;
    private Thread listenThread;

    public static string latestMsgStatus;

    private void ListenForData()
    {
        // Check if unity is still active
        while (mainThread.IsAlive)
        {
            if (!mainThread.IsAlive)
            {
                SendData("Close This Connection");
                break;
            }

            if (ServerAPI.socket.Connected)
            {
                ProcessData();

                if (!mainThread.IsAlive)
                {
                    SendData("Close This Connection");
                    break;
                }
            }
            System.Threading.Thread.Sleep(LISTEN_INTERVAL);
        }
    }

    private string RecieveData()
    {
        byte[] recvBytes = new byte[5 * 1024];

        if (ServerAPI.socket != null && ServerAPI.socket.Connected)
        {
            try
            {
                int bytesRec = socket.Receive(recvBytes);
                return System.Text.Encoding.UTF8.GetString(recvBytes);
            } catch (Exception e)
            {
                if (!mainThread.IsAlive)
                {
                    System.Threading.Thread.CurrentThread.Abort();
                    SendData("Close This Connection");
                }
                Debug.Log(e.StackTrace);
            } 
            //Debug.Log(System.Text.Encoding.UTF8.GetString(recvBytes));
        }
        return "";
    }

    private void ProcessData()
    {
        string dataAsString = RecieveData();

        string[] dataAsArray = dataAsString.Split('`');

        if (!mainThread.IsAlive)
        {
            SendData("Close This Connection");
            return;
        }

        foreach (string s in dataAsArray)
        {
            if (!s.Replace("\n", "").Trim().Equals(""))
                Debug.Log("Msg Recieved: " + s);

            if (s.Contains("Process_ID:"))
            {
                Proc_Id = s.Substring(12);
                Debug.Log("My process id is " + Proc_Id);
            }
            else if (s.Contains("Ping!"))
            {
                if (Proc_Id != null)
                {
                    SendData("Process_ID: " + Proc_Id);
                }
                else
                {
                    SendData("Process_ID: NULL");
                }
            }
            else if (s.Contains("Register Success"))
            {
                Debug.Log("This client has been registered!");
                latestMsgStatus = "Register Success";
            }
            else if (s.Contains("Register Failure"))
            {
                Debug.Log("The username may have been taken already :(");
                latestMsgStatus = "Register Failure";
            }
            else if (s.Contains("Game Started"))
            {
                GameManager.switchToGame = true;
                GameManager.gameStarted = true;
                Debug.Log("Game has begun...");
            }
            else if (s.Contains("JSON DATA STATS: "))
            {
                // Parsing JSON data like the accuracy, wpm, etc...
                string jsonAsString = s.Substring(17);

                ClientData cl = JsonConvert.DeserializeObject<ClientData>(jsonAsString);

                if (cl != null)
                {
                    ClientData c;
                    if (GameManager.playerStats.TryGetValue(cl.Name, out c))
                    {
                        GameManager.playerStats.Remove(cl.Name);

                    }

                    GameManager.playerStats.Add(cl.Name, cl);
                }
                GameManager.infoUpdateNeeded = true;
            }
            else if (s.Contains("Game will start in (seconds): "))
            {
                int time = int.Parse(s.Substring(30));
                GameManager.timerFinished = false;
                GameManager.time = time;
                GameManager.timerBeforeGameStartsStarted = true;
            }
            else if (s.Contains("Game Completed"))
            {
                GameManager.gameComplete = true;
                GameManager.gameStarted = false;
                Debug.Log("Game has been completed");
            }
            else if (s.Contains("Word List: "))
            {
                string wordList = s.Substring(11);
                GameManager.wordList = wordList;
            }
            else if (s.Contains("JSON DATA CONNINFO: "))
            {
                string jsonAsString = s.Substring(20);
                ConnectionInfo info = JsonConvert.DeserializeObject<ConnectionInfo>(jsonAsString);
                GameManager.conInfo = info;
                GameManager.updateConInfo = true;
            }
            else if (s.Contains("Are you alive?"))
            {
                if (mainThread.IsAlive)
                {
                    SendData("I am alive");
                }
                else
                {
                    SendData("Close this Connection");
                }
            }
            else if (s.Contains("Server Msg: "))
            {
                string msg = s.Substring(12);
                GameManager.serverMsg = msg;
                GameManager.newServerMsg = true;
            }
            else if (s.Contains("Time Left: "))
            {
                string time = s.Substring(11);
                GameManager.startTime = (int)float.Parse(time);
            }
            else if (s.Contains("Added to the solo queue"))
            {
                GameManager.soloGame = true;
            }
            else
            {
                if (!s.Replace("\n", "").Trim().Equals(""))
                    Debug.Log("Don't know how to process this data: " + s);
            }
        }
    }

    private void Update()
    {
    }

    private void Start()
    {
        ConnectToServer();
        //SendData("Register Username: Testing12345");

        listenThread = new Thread(ListenForData);
        //listenThread.IsBackground = true;
        listenThread.Start();
    }

    public static void SendData(string data)
    {
        if (ServerAPI.socket != null && ServerAPI.socket.Connected)
        {
            try
            {
                // ` = End of line
                byte[] ByteRepresentation = System.Text.Encoding.UTF8.GetBytes(data + "`");
                socket.Send(ByteRepresentation);
            }
            catch (Exception e)
            {
                Debug.Log(e.StackTrace);
            }
        }
        else
        {
            Debug.Log("Not connected to the Server");
        }
    }

    public static bool ConnectToServer()
    {
        IPEndPoint hostEndPoint;
        IPAddress hostAddress = null;
        int conPort = 5014;

        // Get DNS host information.
        IPHostEntry hostInfo = Dns.GetHostEntry("potatoserverdns.ddns.net");
        // Get the DNS IP addresses associated with the host.
        IPAddress[] IPaddresses = hostInfo.AddressList;

        // Evaluate the socket and receiving host IPAddress and IPEndPoint.
        for (int index = 0; index < IPaddresses.Length; index++)
        {
            hostAddress = IPaddresses[index];
            hostEndPoint = new IPEndPoint(hostAddress, conPort);

            // Creates the Socket to send data over a TCP connection.
            socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);

            // Connect to the host using its IPEndPoint.
            socket.Connect(hostEndPoint);

            if (!socket.Connected)
            {
                // Connection failed, try next IPaddress.
                socket = null;
                continue;
            } else
            {
                GameManager.connectedToServer = true;

                
                return true;
            }
        }

        GameManager.connectedToServer = false;
        return false;
    }
}

public class ConnectionInfo
{
    private int totalConnections;
    private int easyQueueConnections;
    private int challengeQueueConnections;
    private int insaneQueueConnections;

    public int TotalConnections { get => totalConnections; set => totalConnections = value; }
    public int EasyQueueConnections { get => easyQueueConnections; set => easyQueueConnections = value; }
    public int ChallengeQueueConnections { get => challengeQueueConnections; set => challengeQueueConnections = value; }
    public int InsaneQueueConnections { get => insaneQueueConnections; set => insaneQueueConnections = value; }
}

public class ClientData
{
    private string name;
    private string wpm;
    private string accuracy;
    private string timeLeft;

    public string Name { get => name; set => name = value; }
    public string WPM { get => wpm; set => wpm = value; }
    public string Accuracy { get => accuracy; set => accuracy = value; }
    public string TimeLeft { get => timeLeft; set => timeLeft = value; }
}
