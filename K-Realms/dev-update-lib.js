const fs = require('fs');
const path = require('path');
const axios = require('axios');

const downloadFile = async (url, token, outputDir) => {
    const fileName = path.basename(url);
    const outputPath = path.join(outputDir, fileName);

    try {
        const response = await axios({
            url,
            method: 'GET',
            responseType: 'stream',
            headers: {
                'Authorization': `token ${token}`
            }
        });

        // สร้าง directory หากยังไม่มี
        if (!fs.existsSync(outputDir)) {
            fs.mkdirSync(outputDir, { recursive: true });
        }

        if(fs.existsSync(outputPath)) {
            fs.rmSync(outputPath)
        }

        const writer = fs.createWriteStream(outputPath);

        response.data.pipe(writer);

        return new Promise((resolve, reject) => {
            writer.on('finish', resolve);
            writer.on('error', reject);
        });

    } catch (error) {
        console.error('Error downloading the file:', error.message);
    }
};

const GITHUB_TOKEN = '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'; // แทนที่ด้วย Personal Access Token ของคุณ
const FILE_URL = 'https://raw.githubusercontent.com/itorz7/realms-java/master/K-Core/target/K-Core-1.0.jar'; // ลิงก์ไฟล์ raw
const OUTPUT_DIR = path.join(__dirname, 'libs'); // บันทึกไฟล์ใน libs directory

downloadFile(FILE_URL, GITHUB_TOKEN, OUTPUT_DIR)
    .then(() => console.log('All Library is updated'))
    .catch(err => console.error('Error:', err));
