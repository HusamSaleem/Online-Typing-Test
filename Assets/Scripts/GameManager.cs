using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class GameManager : MonoBehaviour
{
    public static string playerUsername;

    public GameObject gameInfoPanel;
    public GameObject registerPanel;
    public GameObject gamePanel;

    public static bool connectedToServer;
    private bool updatedConStatus = false;

    private bool inQueue = false;
    private int difficulty = -1;
    public static string wordList;

    // Switching between panels, and game starting variables
    public static bool switchToGame = false;
    public static bool timerBeforeGameStartsStarted = false;
    public static bool gameStartedUIUpdate = false;
    public static bool gameStarted = false;
    public static bool infoUpdateNeeded = false;
    public static bool gameComplete = false;

    // Timer variables
    private bool canDecreaseTime = true;
    public static int time = 0;
    public static bool timerFinished = false;
    private bool elapsedTimerStart = false;
    private int elapsedTime = 0;

    public static bool soloGame = false;

    public static ConnectionInfo conInfo;
    public static bool updateConInfo = false;

    public static bool newServerMsg = false;
    public static string serverMsg;

    public static int startTime;

    public static Dictionary<string, ClientData> playerStats = new Dictionary<string, ClientData>();

    [Header("Game Info Panel")]
    public Text usernameText;
    public Text pConnectedText;
    public Text easyQText;
    public Text challengeQText;
    public Text insaneQText;
    public Text gameInfoLog;
    public Text serverLog;

    [Header("Register Panel")]
    public InputField registerUsernameInput;
    public Text log;

    private void Update()
    {
        // Switch to game panel
        if (switchToGame)
        {
            switchToGame = false;
            log.text = "";
            switchToGamePanel();
            gameStartedUIUpdate = true;
        }

        // Cancel the Queue Timer as game has been found
        if (timerBeforeGameStartsStarted && elapsedTimerStart)
        {
            elapsedTime = 0;
            elapsedTimerStart = false;
        }

        // Delay timer starts
        if (timerBeforeGameStartsStarted && canDecreaseTime && !timerFinished)
        {
            StartCoroutine(CountDown());
        }

        if (updateConInfo)
        {
            updateConUI();
        }

        // Queue Timer
        if (elapsedTimerStart && !timerBeforeGameStartsStarted && canDecreaseTime)
        {
            StartCoroutine(ElapseTime());
        }

        if (newServerMsg)
        {
            serverLog.text = "Server Message: " + serverMsg;
            newServerMsg = false;
        }

        if (!connectedToServer && !updatedConStatus)
        {
            log.color = Color.red;
            log.text = "Could not connect to the server\nPlease try again later";
            updatedConStatus = true;
        }
        if (connectedToServer && !updatedConStatus)
        {
            log.color = Color.green;
            log.text = "Server connection established!";
            updatedConStatus = true;
        }
    }

    // Updates the connection information (Queues, etc...)
    private void updateConUI()
    {
        pConnectedText.gameObject.SetActive(true);
        easyQText.gameObject.SetActive(true);
        challengeQText.gameObject.SetActive(true);
        insaneQText.gameObject.SetActive(true);

        pConnectedText.text = "Players Connected: " + conInfo.TotalConnections;
        easyQText.text = "Players in Easy Queue: " + conInfo.EasyQueueConnections;
        challengeQText.text = "Players in Challenge Queue: " + conInfo.ChallengeQueueConnections;
        insaneQText.text = "Players in Insane Queue: " + conInfo.InsaneQueueConnections;

        updateConInfo = false;
    }

    // Tries to register the username
    public void tryRegister()
    {
        if (ServerAPI.socket == null || !ServerAPI.socket.Connected)
        {
            log.text = "Unable to connect to the server...";
            return;
        }

        string name = registerUsernameInput.text;
        if (!checkUsername(name))
        {
            log.color = Color.red;
            log.text = "Name is too long or too short...\n (3-16 characters)";
            log.color = Color.yellow;

            return;
        }

        log.text = "Attempting to register " + name + "...";
        System.Threading.Thread.Sleep(500);

        ServerAPI.SendData("Register Username: " + name);

        int retryTimes = 0;
        bool success = false;

        while (retryTimes < 3)
        {
            if (ServerAPI.latestMsgStatus != null)
            {
                if (ServerAPI.latestMsgStatus.Equals("Register Success"))
                {
                    Debug.Log("Successfully registered");
                    log.color = Color.green;
                    log.text = "Successfully Registered!";
                    playerUsername = name.ToLower();
                    success = true;
                    break;
                }
                else if (ServerAPI.latestMsgStatus.Equals("Register Failure"))
                {
                    Debug.Log("Registration failure");
                    log.color = Color.red;
                    log.text = "Failed to register the username\n";
                    log.text += "Most likely because the username is taken already";
                    break;
                }
            }

            log.text = "Trying to recieve data from the server";
            System.Threading.Thread.Sleep(1000);
            retryTimes++;
        }

        ServerAPI.latestMsgStatus = null;
        if (success)
        {
            switchToGameInfo();
            updateGameInfo();
            getConnectionInfo();
        }
        else
        {
            log.color = Color.red;
            log.text = "Failed to register the username (Username is taken)";
            log.text += "\n Please try again...";
        }
    }

    private bool checkUsername(string name)
    {
        if (name.Trim().Length < 3 || name.Trim().Length > 16 || name.Trim().Equals(""))
        {
            return false;
        }

        return true;
    }

    public void getConnectionInfo()
    {
        ServerAPI.SendData("Require Info: Connections");
    }

    public IEnumerator ElapseTime()
    {
        gameInfoLog.text = elapsedTime + " seconds in Queue...";
        elapsedTime++;
        canDecreaseTime = false;
        yield return new WaitForSeconds(1);
        canDecreaseTime = true;
    }

    public IEnumerator CountDown()
    {
        gameInfoLog.text = time + " seconds left before the game starts!";

        canDecreaseTime = false;
        time--;

        if (time == 0)
        {
            timerFinished = true;
            timerBeforeGameStartsStarted = false;
            ServerAPI.SendData("Ready");
            gameInfoLog.text = "Game is starting!";
        }

        yield return new WaitForSeconds(1);
        canDecreaseTime = true;
    }

    public void updateGameInfo()
    {
        usernameText.text = "Username: " + playerUsername;
    }

    public void switchToGameInfo()
    {
        registerPanel.SetActive(false);
        gamePanel.SetActive(false);
        gameInfoPanel.SetActive(true);
        soloGame = false;
    }

    public void switchToGamePanel()
    {
        registerPanel.SetActive(false);
        gameInfoPanel.SetActive(false);
        gamePanel.SetActive(true);
        resetVariables();
    }

    private void resetVariables()
    {
        StopAllCoroutines();

        elapsedTime = 0;
        time = 0;
        canDecreaseTime = true;
        timerBeforeGameStartsStarted = false;
        timerFinished = false;
        elapsedTimerStart = false;

        difficulty = -1;
        infoUpdateNeeded = false;
        inQueue = false;
    }

    public void quitGame()
    {
        switchToGameInfo();
    }

    public void setDifficulty(int difficulty)
    {
        this.difficulty = difficulty;
    }

    public void stopQueue()
    {
        if (inQueue && !timerBeforeGameStartsStarted)
        {
            difficulty = -1;
            gameInfoLog.text = "The Queue has been stopped";
            ServerAPI.SendData("Leave Queue");
            resetVariables();
        }
        else
        {
            if (timerBeforeGameStartsStarted)
            {
                gameInfoLog.text = "Too late to exit the Queue now.";
            }
            else
            {
                gameInfoLog.text = "You are not in a Queue!";
            }
        }
    }

    public void startSoloGame()
    {
        if (difficulty == -1)
        {
            gameInfoLog.text = "You must choose a difficulty!";
            return;
        }

        if (!inQueue)
        {
            if (gameStarted)
            {
                gameInfoLog.text = "You must wait until the current game you left finishes...";
                return;
            }
            ServerAPI.SendData("Join 1 player queue: " + difficulty);
            inQueue = true;
            gameInfoLog.text = "You have joined the Queue...";
            elapsedTimerStart = true;
        }
        else
        {
            gameInfoLog.text = "You are already in a Queue";
        }
    }

    public void joinQueue()
    {
        if (difficulty == -1)
        {
            gameInfoLog.text = "You must choose a difficulty!";
            return;
        }

        if (!inQueue)
        {
            if (gameStarted)
            {
                gameInfoLog.text = "You must wait until the current game you left finishes...";
                return;
            }
            ServerAPI.SendData("Join 2 player queue: " + difficulty);
            inQueue = true;
            gameInfoLog.text = "You have joined the Queue...";
            elapsedTimerStart = true;
        }
        else
        {
            gameInfoLog.text = "You are already in a Queue";
        }
    }
}
