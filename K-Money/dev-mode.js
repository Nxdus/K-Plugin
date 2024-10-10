const fs = require('fs');
const path = require('path');
const FormData = require('form-data');
const axios = require("axios");

const reloadType = "restart" // "restart" | "plugman"
const reloadCore = true

const serverIds = [
    // '11cf0fd1', // velocity
    // '7f572573', // pre-auth
    '65cc8ea8', // spawn
    'eb7ed8ed', // realm base
    'e339c830', // realm generator
    // 'ec85ca76', // KAK
];

async function getSignedUrl(serverId) {
    const response = await axios.get(`https://panel.labit.wtf/api/client/servers/${serverId}/files/upload`, {
        headers: {
            Authorization: 'Bearer ptlc_hPM1iyLTeWPRzcxle7xRdWP0aGTukQeDXuDylfq4RKM'
        }
    })

    if (response.status !== 200) {
        throw new Error("getSignedUrl()")
    }

    return response.data.attributes.url + `&directory=%2Fplugins`;
}

async function uploadFile(serverId, file) {
    const signedUrl = await getSignedUrl(serverId)
    console.log('File uploading: ' + serverId);

    const data = new FormData();
    data.append('files', fs.createReadStream(file));

    const response = await axios.post(signedUrl, data, {
        headers: {
            ...data.getHeaders(),
        }
    });

    if (response.status !== 200) {
        throw new Error(`Error uploading file: ${response.data.toString()}`);
    }

    console.log('File uploaded successfully: ' + serverId);
}

async function restart(plugin, serverId) {
    if (reloadType === "restart") {
        await axios.post(`https://panel.labit.wtf/api/client/servers/${serverId}/power`, {
            signal: 'restart'
        }, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: 'Bearer ptlc_hPM1iyLTeWPRzcxle7xRdWP0aGTukQeDXuDylfq4RKM'
            }
        }).catch(err => {
            console.log(err.response.data)
        })
    }

    if (reloadType === "plugman") {
        await axios.post(`https://panel.labit.wtf/api/client/servers/${serverId}/command`, {
            command: `plugman reload ${plugin}`
        }, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: 'Bearer ptlc_hPM1iyLTeWPRzcxle7xRdWP0aGTukQeDXuDylfq4RKM'
            }
        }).catch(err => {
            console.log(err.response.data)
        })
    }
}

function debounce(fn, delay) {
    let timeoutId;
    return function (...args) {
        if (timeoutId) {
            clearTimeout(timeoutId);
        }
        timeoutId = setTimeout(() => fn(...args), delay);
    };
}

const debouncedUpload = debounce(async () => {
    await run()
}, 2000);


fs.watch(path.join(__dirname, 'target', 'K-Money-1.0.jar'), (eventType, filename) => {
    if (eventType === 'change' || eventType === 'rename') {
        debouncedUpload();
    }
});

const run = async () => {
    for (const serverId of serverIds) {
        try {

            await uploadFile(serverId, path.join(__dirname, 'target', 'K-Money-1.0.jar'));

            if(reloadCore) {
                await uploadFile(serverId, path.join(__dirname, 'libs', 'K-Core-1.0.jar'));
                await restart('K-Core', serverId)
            }

        } catch (error) {
            console.error(`Error for server ${serverId}:`, error);
        }
    }
}

run()

console.log(`Watching here !`);
